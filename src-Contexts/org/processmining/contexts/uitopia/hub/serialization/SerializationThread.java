package org.processmining.contexts.uitopia.hub.serialization;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPOutputStream;

import org.processmining.contexts.uitopia.hub.ProMResourceManager;
import org.processmining.contexts.uitopia.hub.serialization.IDReferencePair.MType;
import org.processmining.contexts.uitopia.hub.serialization.IDResourcePair.QType;
import org.processmining.contexts.uitopia.hub.serialization.ProMReferenceMarshaller.ReferenceListener;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.ProMID;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.util.WeakValueHashMap;

public class SerializationThread extends Thread implements ProMResource.Listener, ReferenceListener {

	private ObjectOutputStream objectStream = null;
	private ObjectOutputStream indexStream = null;
	private BlockingQueue<IDResourcePair> queue;

	private final ProMResourceManager manager;
	private final Object lock = new Object();
	private ProMResource<?> referencingContext;

	private boolean closing = false;
	private final Object closingLock = new Object();

	private WeakValueHashMap map = new WeakValueHashMap();
	private boolean firstKey = false;
	private FileOutputStream objectZipStream;
	private FileOutputStream indexZipStream;
	private WeakHashMap<ProMResource<?>, ProMResource<?>> serializeOnExit = new WeakHashMap<ProMResource<?>, ProMResource<?>>();
	private boolean running;

	public SerializationThread(ProMResourceManager manager) {
		super();
		this.manager = manager;
		this.setDaemon(false);
		this.setPriority(NORM_PRIORITY - 2);

	}

	public void start() {
		running = true;
		try {
			objectZipStream = new FileOutputStream(
					SerializationConstants.getFile(SerializationConstants.OBJECTPERSISTENCYFILE));
			objectStream = SerializationConstants.createXStream(this).createObjectOutputStream(
					new BufferedOutputStream(new GZIPOutputStream(objectZipStream)));

			indexZipStream = new FileOutputStream(
					SerializationConstants.getFile(SerializationConstants.REMOVEDPERSISTENCYFILE));
			indexStream = SerializationConstants.createXStream().createObjectOutputStream(
					new BufferedOutputStream(new GZIPOutputStream(indexZipStream)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		// Close the stream after shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					// make sure all queues are empty
					System.out.println("Waiting for serialization queues to be written to disk");
					for (ProMResource<?> resource : serializeOnExit.keySet()) {
						if (!resource.isDestroyed()) {
							registerResourceForImmediateSerialization(resource);
						}
					}

					closing = true;
					synchronized (lock) {
						lock.notify();
					}
					while (true) {
						synchronized (closingLock) {
							try {
								closingLock.wait();
								break;
							} catch (InterruptedException e) {

							}
						}
					}
				} finally {
					terminate();
				}

			}

		});

		// Now write the already available resources;
		queue = new LinkedBlockingQueue<IDResourcePair>();
		super.start();
		if (Boot.VERBOSE == Level.ALL) {
			System.out.println("Serialization thread Alive");
		}
	}

	public void registerResourceForImmediateSerialization(ProMResource<?> resource) {
		try {
			if (queue != null) {
				removePending(resource.getID());
				this.queue.put(new IDResourcePair(resource.getID(), resource, QType.SERIALIZE));
				synchronized (lock) {
					lock.notify();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void registerResourceForSerializationOnExit(ProMResource<?> resource) {
		serializeOnExit.put(resource, resource);
	}

	public void unRegisterScheduledResource(ProMID id) {
		try {
			if (queue != null) {
				removePending(id);
				IDResourcePair pair = new IDResourcePair(id, null, QType.DELETE);
				this.queue.put(pair);
				synchronized (lock) {
					lock.notify();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void changed(ProMResource<?> resource, boolean fullUpdate) {
		try {
			if (queue != null) {
				if (fullUpdate) {
					removePending(resource.getID());
					// signal a removal
					this.queue.put(new IDResourcePair(resource.getID(), resource, QType.DELETE));
					// and an insert
					this.queue.put(new IDResourcePair(resource.getID(), resource, QType.SERIALIZE));
				} else {
					this.queue.put(new IDResourcePair(resource.getID(), resource, QType.UPDATE));
				}
				synchronized (lock) {
					lock.notify();
				}
			}
			resource.setListener(this);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void terminate() {
		running = false;
		// should terminate
		try {
			objectStream.flush();
			objectStream.close();
			System.out.println("Object stream closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			indexStream.flush();
			indexStream.close();
			System.out.println("Index stream closed");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Serialization queues written to disk");

	}

	private void removePending(ProMID id) {
		synchronized (queue) {
			Iterator<IDResourcePair> it = queue.iterator();
			while (it.hasNext()) {
				IDResourcePair pair = it.next();
				if (pair.id.equals(id)) {
					it.remove();
				}
			}
		}
	}

	public void run() {

		try {
			while (true) {

				while (!queue.isEmpty()) {
					IDResourcePair pair = queue.take();
					if (pair.type == QType.SERIALIZE) {
						serialize(pair.resource);
					} else if (pair.type == QType.DELETE) {
						remove(pair.id);
					} else {
						update(pair.resource);
					}
				}
				if (closing && queue.isEmpty()) {
					while (running) {
						synchronized (closingLock) {
							// notify the shutdown hook waiting on this object.
							closingLock.notify();
						}
						Thread.sleep(1000);
					}
					return;
				}
				synchronized (lock) {
					lock.wait();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			terminate();
		}

	}

	private void update(ProMResource<?> resource) throws IOException {

		referencingContext = resource;
		long t = System.currentTimeMillis();
		objectStream.writeObject(new SerializedResource.Update(resource));
		if (Boot.VERBOSE == Level.ALL) {
			System.out.println("   Updated " + resource.getName() + "(in " + (System.currentTimeMillis() - t) / 1000
					+ " seconds)");
		}
		objectStream.flush();
		referencingContext = null;
	}

	private void remove(ProMID id) throws IOException {
		long t = System.currentTimeMillis();
		indexStream.writeObject(new IDReferencePair(id.getUUID(), MType.DELETE));
		if (Boot.VERBOSE == Level.ALL) {
			System.out.println("   Removed " + id + "(in " + (System.currentTimeMillis() - t) / 1000 + " seconds)");
		}
		indexStream.flush();
	}

	private void serialize(ProMResource<?> resource) throws IOException {
		if (resource.isDestroyed()) {
			return;
		}
		long t = System.currentTimeMillis();
		resource.setListener(this);
		referencingContext = resource;
		firstKey = true;
		if (Boot.VERBOSE == Level.ALL) {
			System.out.println("   Starting serialization of " + resource.getName());
		}
		try {
			objectStream.writeObject(new SerializedResource(resource));
			if (Boot.VERBOSE == Level.ALL) {
				System.out.println("   Serialized " + resource.getName() + "(in " + (System.currentTimeMillis() - t)
						/ 1000 + " seconds)");
			}
		} catch (Exception e) {
			// something went wrong
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("   Serialized " + resource.getName() + " failed after "
						+ (System.currentTimeMillis() - t) / 1000 + " seconds");
				e.printStackTrace();
			}

		}
		objectStream.flush();
		objectZipStream.getFD().sync();
		referencingContext = null;
	}

	public void referenceAdded(Object existingReferenceKey) {
		try {
			writeKey((UUID) map.get(existingReferenceKey), existingReferenceKey);

			indexStream.writeObject(new IDReferencePair(referencingContext.getID().getUUID(), MType.REFERENCE,
					existingReferenceKey));
			//			if (Boot.VERBOSE == Level.ALL) {
			//				System.out.println("   Key added: " + existingReferenceKey + " of resource: "
			//						+ map.get(existingReferenceKey));
			//				System.out.println("   Reference added: " + existingReferenceKey + " to resource: "
			//						+ referencingContext);
			//			}
			indexStream.flush();
			indexZipStream.getFD().sync();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeKey(UUID uuid, Object referenceKey) throws IOException {
		indexStream.writeObject(new IDReferencePair(uuid, MType.KEY, referenceKey));
	}

	public void keyCreated(Object referenceKey) {
		if (firstKey) {
			try {
				writeKey(referencingContext.getID().getUUID(), referenceKey);
				indexStream.flush();
				indexZipStream.getFD().sync();
				firstKey = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		map.put(referenceKey, referencingContext.getID().getUUID());
	}
}

class IDResourcePair {

	static enum QType {
		DELETE, SERIALIZE, UPDATE
	};

	public ProMID id;

	public ProMResource<?> resource;

	QType type;

	public IDResourcePair(ProMID id, ProMResource<?> resource, QType type) {
		this.id = id;
		this.resource = resource;
		this.type = type;

	}

	public boolean equals(Object o) {
		if (o instanceof IDResourcePair) {
			IDResourcePair res = (IDResourcePair) o;
			return res.id.equals(id) && res.type == type;
		}
		return false;
	}
}

class IDReferencePair {

	static enum MType {
		DELETE, REFERENCE, KEY
	};

	public UUID id;

	public MType type;

	public Object referenceKey = null;

	public IDReferencePair(UUID id, MType type) {
		this.id = id;
		this.type = type;
	}

	public IDReferencePair(UUID id, MType type, Object referenceKey) {
		this(id, type);
		this.referenceKey = referenceKey;
	}

	public String toString() {
		return id.toString() + " " + type + " " + referenceKey.toString();
	}

}