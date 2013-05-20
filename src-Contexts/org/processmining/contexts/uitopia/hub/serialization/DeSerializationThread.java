package org.processmining.contexts.uitopia.hub.serialization;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.deckfour.uitopia.api.model.ResourceType;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.hub.overlay.ProgressOverlayDialog;
import org.processmining.contexts.uitopia.hub.serialization.IDReferencePair.MType;
import org.processmining.contexts.uitopia.hub.serialization.SerializedResource.Update;
import org.processmining.contexts.uitopia.model.ProMPOResource;
import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.ProMID;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.providedobjects.ContextAwareObject;
import org.processmining.framework.providedobjects.ProvidedObjectID;
import org.processmining.framework.providedobjects.ProvidedObjectManager;

import com.thoughtworks.xstream.io.StreamException;

public class DeSerializationThread extends SwingWorker<Object, Object> {

	private int steps = 100;
	private long t;
	private ProgressOverlayDialog dialog;
	private SerializationCancellationListener listener;
	private final UIContext context;

	public DeSerializationThread(UIContext context, String progressLabel) {
		this.context = context;
		t = System.currentTimeMillis();
		listener = new SerializationCancellationListener();
		dialog = new ProgressOverlayDialog(context.getController().getMainView(), progressLabel, listener);
		dialog.setIndeterminate(false);
		context.getController().getMainView().showOverlay(dialog);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doInBackground() throws Exception {

		File f = SerializationConstants.getFile(SerializationConstants.REMOVEDPERSISTENCYFILE);
		//Set<UUID> deleted = new HashSet<UUID>();
		Map<UUID, Set<Object>> usedRefs = new HashMap<UUID, Set<Object>>();
		Map<Object, UUID> keys = new HashMap<Object, UUID>();
		Map<UUID, Object> firstKey = new HashMap<UUID, Object>();
		ObjectInputStream in = null;
		try {
			in = SerializationConstants.createXStream().createObjectInputStream(
					new GZIPInputStream(new FileInputStream(f)));
			// first read the deleted IDs
			while (true) {
				IDReferencePair pair = (IDReferencePair) in.readObject();
				UUID id = pair.id;
				Object reference = pair.referenceKey;
				if (pair.type == MType.DELETE) {
					//deleted.add(id);
					// clear the used referenced of id.
					usedRefs.put(id, new HashSet<Object>());
					firstKey.remove(id);
				} else if (pair.type == MType.KEY) {
					keys.put(reference, id);
					if (!usedRefs.containsKey(id)) {
						usedRefs.put(id, new HashSet<Object>());
						firstKey.put(id, reference);
					}
				} else {
					Set<Object> s = usedRefs.get(id);
					s.add(reference);
					UUID uuid = keys.get(reference);
					s.addAll(usedRefs.get(uuid));
				}
			}
		} catch (EOFException e) {
			// End of File reached.
		} catch (StreamException e) {
			// problem with the stream
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
		}

		Set<Object> referencesToKeep = new HashSet<Object>();
		for (Set<Object> refs : usedRefs.values()) {
			referencesToKeep.addAll(refs);
		}
		referencesToKeep.addAll(firstKey.values());
		if (Boot.VERBOSE == Level.ALL) {
			System.out.println("Keeping references: " + referencesToKeep);
		}
		if (referencesToKeep.isEmpty()) {
			// Done
			return null;
		}

		f = SerializationConstants.getFile(SerializationConstants.OBJECTPERSISTENCYFILE);
		XMonitoredInputStream s;
		int progress = 1;
		s = new XMonitoredInputStream(f, new XProgressListener() {

			public void updateProgress(final int p, int maxProgress) {
				if (dialog.getMaximum() != maxProgress) {
					dialog.changeProgressBounds(0, maxProgress + steps);
				}
				dialog.changeProgress(p);
			}

			public boolean isAborted() {
				return listener.isCanceled();
			}

		});
		progress = s.getStepNumber();

		ProvidedObjectManager manager = context.getProvidedObjectManager();

		if (listener.isCanceled()) {
			return null;
		}
		dialog.changeProgressCaption("Reading persistency file...");
		in = null;
		try {
			in = SerializationConstants.createXStream(referencesToKeep).createObjectInputStream(new GZIPInputStream(s));
		} catch (EOFException e) {
			// End of File reached.
			return null;
		}

		Map<ProMID, SerializedResource> poList = new HashMap<ProMID, SerializedResource>();
		List<Connection> coList = new ArrayList<Connection>();
		Map<ProMID, SerializedResource.Update> updates = new HashMap<ProMID, SerializedResource.Update>();

		try {
			while (true) {
				if (listener.isCanceled()) {
					break;
				}
				try {
					Object o = in.readObject();
					if (o instanceof SerializedResource) {
						SerializedResource resource = (SerializedResource) o;
						if (!firstKey.containsKey(resource.id.getUUID())) {
							continue;
						}
						// This resource needs to be added.
						if (resource.id instanceof ProvidedObjectID) {
							poList.put(resource.id, resource);
						} else {
							coList.add((Connection) resource.instance);
						}
					} else {
						SerializedResource.Update update = (SerializedResource.Update) o;
						updates.put(update.id, update);
					}

				} catch (ClassNotFoundException e) {
					// Cannot find the class of o, hence, cannot deserialize it
					continue;
				} catch (EOFException e) {
					// End of file reached, continue regular execution
					break;
				} catch (Exception e) {
					System.err.println("Error in deserialization: " + e.getMessage());
					e.printStackTrace();
					System.err.println("Continuing with objects so far");

					break;
				}
			}
		} finally {
			in.close();
			s.close();
			if (Boot.DO_SERIALIZATION) {
				context.getResourceManager().getSerializationThread().start();
			}
		}
		if (poList.isEmpty()) {
			return null;
		}
		dialog.changeProgressCaption("Restoring workspace...");

		int inc = Math.max(steps / (2 * poList.size() + coList.size()), 1);

		Map<ProMID, ProMResource<?>> oldID2newResource;
		Map<ProMID, ProMID> newID2oldID;
		newID2oldID = new HashMap<ProMID, ProMID>();
		oldID2newResource = new HashMap<ProMID, ProMResource<?>>();

		// First, deserialize and reinitialize the provided objects
		for (SerializedResource resource : poList.values()) {
			if (listener.isCanceled()) {
				return null;
			}
			Object object = resource.instance;
			if (object instanceof ContextAwareObject) {
				ContextAwareObject cao = (ContextAwareObject) object;
				cao.setManagers(context.getConnectionManager(), context.getPluginManager(),
						context.getProvidedObjectManager());
			}
			ResourceType rt = context.getResourceManager().getResourceTypeFor(resource.type);
			if (rt == null) {
				// The serialized resource type is not supported.
				continue;
			}
			ProMID newID = manager.createProvidedObject(resource.name, object, rt.getTypeClass(),
					context.getMainPluginContext());
			newID2oldID.put(newID, resource.id);
			progress += inc;
			dialog.changeProgress(progress);
		}
		// Now, check all resources to map from new POs to resource 
		for (ProMResource<?> resource : context.getResourceManager().getAllResources()) {
			oldID2newResource.put(newID2oldID.get(resource.getID()), resource);
		}
		for (SerializedResource oldResource : poList.values()) {
			ProMResource<?> resource = oldID2newResource.get(oldResource.id);
			String name = resource.getName();
			boolean favorite = oldResource.favorite;

			if (updates.containsKey(oldResource.id)) {
				Update u = updates.get(oldResource.id);
				name = u.name;
				favorite = u.favorite;
			}

			resource.setFavorite(favorite);
			resource.setName(name);

			resource.setCreationTime(oldResource.creationTime);
			Set<ProMPOResource> parents = new HashSet<ProMPOResource>();
			for (ProMID id : oldResource.parents) {
				// if this id was a parent, then it is a provided object,
				// not a connection;
				parents.add((ProMPOResource) oldID2newResource.get(id));
			}
			resource.setParents(parents);
			progress += inc;
			dialog.changeProgress(progress);
		}

		for (Connection c : coList) {
			if (listener.isCanceled()) {
				return null;
			}
			context.getMainPluginContext().addConnection(c);
			progress += inc;
			dialog.changeProgress(progress);
		}
		return null;
	}

	@Override
	protected void done() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				context.getController().getMainView().getWorkspaceView().updateResources();
				dialog.changeProgress(dialog.getMaximum());
				context.getController().getMainView().hideOverlay();
			}
		});

		try {
			get();
		} catch (Exception e) {
			System.err.println("deserialization Error: " + e.getMessage());
			e.printStackTrace();
		}

		if (Boot.DO_SERIALIZATION && !context.getResourceManager().getSerializationThread().isAlive()) {
			context.getResourceManager().getSerializationThread().start();
		}

		System.out.println("");
		System.out.println("deserialization took: " + (System.currentTimeMillis() - t) / 1000 + " seconds");
	}

}
