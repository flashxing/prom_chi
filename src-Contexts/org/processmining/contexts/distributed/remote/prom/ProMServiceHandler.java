package org.processmining.contexts.distributed.remote.prom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlSerializer;
import org.processmining.contexts.distributed.DistributedPluginContext;
import org.processmining.contexts.distributed.GridMiddlewareLogs;
import org.processmining.contexts.distributed.LogListener;
import org.processmining.contexts.distributed.PluginListenerDistributedImpl;
import org.processmining.contexts.distributed.remote.ProMProtocol;
import org.processmining.contexts.distributed.remote.RemoteProvidedObject;
import org.processmining.contexts.distributed.remote.ServiceHandler;
import org.processmining.contexts.distributed.remote.TransferHelpFunctions;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.providedobjects.ProvidedObjectDeletedException;
import org.processmining.framework.providedobjects.ProvidedObjectID;

/**
 * This class implements an example Service Handler
 * 
 * @author cbratosi
 * 
 */

public class ProMServiceHandler implements ServiceHandler {

	//This is the main context

	//Since here we do all the work here we have to have also everything
	//related to run a plugin and adding removing things from the provided 
	//objects

	//TODO 
	//Can this class be re-used also for communication plugins????

	DistributedPluginContext globalContext;

	public ProMServiceHandler(DistributedPluginContext distributedPluginContext) {
		// TODO Auto-generated constructor stub
		globalContext = distributedPluginContext;
	}

	public void handleRequest(BufferedReader in, PrintWriter out) {
		// TODO Auto-generated method stub
		System.out.println("A client called");
		String line = null;

		TransferHelpFunctions.writeLine(out, ProMProtocol.HELO_SERVER);
		try {
			while ((line = TransferHelpFunctions.readLine(in)) != null) {
				//here each message is received and treated accordingly
				System.out.println("Client:" + line);

				/*
				 * Client sent goodbye? If yes that was it Otherwise say to the
				 * client that alles goed
				 */
				if (line.equals(ProMProtocol.CLIENT_GOODBYE)) {
					TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_GOODBYE);
					break;
				} else {
					TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_REQUEST_RECEIVED);
				}

				//is not a good bye
				//treat(line);

				if (line.equalsIgnoreCase(ProMProtocol.CLIENT_SEND_LOG_REQUEST)) {
					//Client wants to send some data

					//System.out.println("Client sends a log:" + line);
					ProvidedObjectID id = receiveLog(in);
					//System.out.println("The object id is "+id.toString());

					String s = TransferHelpFunctions.serialize(id);

					TransferHelpFunctions.writeXML(out, s);

					//System.out.println("The object id after is "+ deserialize(s).toString());
					//we send the Provided Object ID to the client

				} else {

					if (line.equalsIgnoreCase(ProMProtocol.CLIENT_SEND_DATA_REQUEST)) {
						//Client wants to send some data

						//System.out.println("Client:" +line);
						ProvidedObjectID id = receiveObject(in);
						//System.out.println("The object id is "+id.toString());

						String s = TransferHelpFunctions.serialize(id);

						TransferHelpFunctions.writeXML(out, s);

						//System.out.println("The object id after is "+ deserialize(s).toString());
						//we send the Provided Object ID to the client

					} else {
						if (line.equalsIgnoreCase(ProMProtocol.CLIENT_RECEIVE_DATA_REQUEST)) {
							//Client wants to receive some data

							// First we get the PO id

							line = TransferHelpFunctions.receiveXML(in);

							//System.out.println("Client: "+ line);

							Object o = TransferHelpFunctions.deserialize(line);

							if (isParameterAssignable(o.getClass(), ProvidedObjectID.class)) {
								//everything ok
								//System.out.println("I have the object requested");
								sendObject(out, (ProvidedObjectID) o);
							} else {
								//System.out.println("Server: the data is not a PO ID type");
								TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
							}

						} else {
							if (line.equalsIgnoreCase(ProMProtocol.CLIENT_RUN_PLUGIN_REQUEST)) {
								//Client wants to run a plugin
								//System.out.println("Client wants to run a plugin");
								//First we receive the name of the plugin and we have to check if this plugin 
								//exists in the framework

								line = TransferHelpFunctions.readLine(in);

								Collection<PluginDescriptor> pd = globalContext.getPluginManager().getAllPlugins();
								boolean exists = false;

								for (PluginDescriptor p : pd) {
									//System.out.println(p.getName());
									if (p.getName().equalsIgnoreCase(line)) {
										exists = true;
										break;
									}
								}

								//								if (exists) {
								//									//System.out.println("I found the plugin");
								//								} else {
								//									//System.out.println("I didin't find the plugin");
								//								}

								if (exists) {
									// Then we have to ask the client to send the input data
									// The response can be: no parameters required, a list of parameters
									// Each parameter can be a PO ID or another type of object

									String pluginName = line;

									TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_SEND_PARAMETERS);

									List<Object> args = new ArrayList<Object>();

									while (true) {

										line = TransferHelpFunctions.receiveXML(in);

										//System.out.println("Parameter " + line);

										if (line.equalsIgnoreCase(ProMProtocol.CLIENT_FINISHED_PARAMETERS)) {
											break;
										}

										Object o = TransferHelpFunctions.deserialize(line);

										//System.out.println("received object: "+ line);

										//now here i have a problem
										//because I have to say when i send an object and when a PO_ID
										if (isParameterAssignable(o.getClass(), ProvidedObjectID.class)) {
											//this is a provided object
											//find first if it is a valid provided object 
											//and after that pick it up
											o = globalContext.getProvidedObjectManager().getProvidedObjectObject(
													(ProvidedObjectID) o, true);
											if (o != null) {
												args.add(o);
											} else {
												exists = false;
												break;
											}
											//System.out.println("Object"+o.toString()+ "exists");
											//}

										} else {
											globalContext.getProvidedObjectManager().createProvidedObject(
													"Argument for " + pluginName + " of type "
															+ o.getClass().toString(), o, globalContext);
											args.add(o);
											//System.out.println("Create new id: "+poid.toString());
											//}
										}

									}

									if (exists) {

										// Now we have everything so we send everything to run and we return the PI IDs
										// However this would be nice to be done even if the PO is not there yet
										try {

											//System.out.println("I am running now the plugin");
											Map<Class<?>, ProvidedObjectID> poid = runPlugin(globalContext, pluginName,
													args.toArray());

											TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_SEND_PO_IDS);
											//										Iterator<Map.Entry<Class<?>, ProvidedObjectID>> keyValuePairs = poid.entrySet().iterator();
											//										for (int i = 0;i<poid.size(); i++){
											//											Map.Entry<Class<?>, ProvidedObjectID> entry = (Map.Entry<Class<?>, ProvidedObjectID>) keyValuePairs.next();
											//											//System.out.println("This is the class "+entry.getKey().toString());//this the po id
											//											//System.out.println("This is the POID "+entry.getValue().toString());//this is the class
											//											writeXML(out, serialize(entry));
											//											}
											//										
											//										writeXML(out,Protocol.SERVER_FINISHED_PO_IDS);
											TransferHelpFunctions.writeXML(out, TransferHelpFunctions.serialize(poid));
											//TODO what we do with arrays?????
										} catch (Exception e) {
											e.printStackTrace();
											TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
										}
									} else {
										TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
									}
								} else {
									TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
								}
							} else {
								if (line.equalsIgnoreCase(ProMProtocol.CLIENT_SEND_REMOTE_DATA_REQUEST)) {
									//Client wants to send some data

									//System.out.println("Client:" +line);
									ProvidedObjectID id = receiveRemoteObject(in);
									//System.out.println("The object id is "+id.toString());

									String s = TransferHelpFunctions.serialize(id);

									TransferHelpFunctions.writeXML(out, s);

									//System.out.println("The object id after is "+ deserialize(s).toString());
									//we send the Provided Object ID to the client

								} else {
									if (line.equalsIgnoreCase(ProMProtocol.CLIENT_DISCARD_DATA_REQUEST)) {
										//Client wants to receive some data

										// First we get the PO id

										line = TransferHelpFunctions.receiveXML(in);

										//System.out.println("Client: "+ line);

										Object o = TransferHelpFunctions.deserialize(line);

										if (isParameterAssignable(o.getClass(), ProvidedObjectID.class)) {
											//everything ok
											discardObject(out, (ProvidedObjectID) o);
										} else {
											//System.out.println("Server: the data is not a PO ID type");
											TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
										}

									} else {
										throw new Exception("I don't know what the clients wants");
									}

								}
							}
						}
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void discardObject(PrintWriter out, ProvidedObjectID o) {
		// TODO Auto-generated method stub
		TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_REQUEST_RECEIVED);

		//I have to see how I deal with the ProM Futures

		try {
			globalContext.getProvidedObjectManager().deleteProvidedObject(o);
			TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_DISCARDED_DATA);
		} catch (ProvidedObjectDeletedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
		}

	}

	protected boolean isParameterAssignable(Class<?> instanceType, Class<?> requestedType) {
		if (requestedType.isAssignableFrom(instanceType)) {
			return true;
		}
		if (requestedType.isArray() && requestedType.getComponentType().isAssignableFrom(instanceType)) {
			return true;
		}
		return false;
	}

	/**
	 * This method reacts to the output according to the message instructions
	 * 
	 * @param line
	 * @throws Exception
	 */
	protected String treat(String line) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(line);

		String response = null;

		return response;
	}

	/**
	 * This class executes a Plugin given by its plugin name the one in the
	 * annotation name
	 * 
	 * Also this class receives the parameters needed for the plugin
	 * 
	 * @param parent
	 *            the parent context
	 * @param pluginName
	 * @param args
	 *            the arguments to run the plugin
	 * @throws Exception
	 */
	protected Map<Class<?>, ProvidedObjectID> runPlugin(DistributedPluginContext parent, String pluginName,
			Object[] args) throws Exception {

		//parent.tryToConstructFirstNamedObject(type, name, input)

		// First we create a child context 
		PluginContext childContext = parent.createChildContext(pluginName);

		//System.out.println("I try to run plugin "+pluginName);

		LogListener log = new LogListener();

		childContext.getLoggingListeners().add(log);

		// Now we have to setup the listeners

		PluginListenerDistributedImpl listener = new PluginListenerDistributedImpl(childContext);

		childContext.getPluginLifeCycleEventListeners().add(listener);

		childContext.getPluginLifeCycleEventListeners().firePluginCreated(childContext);

		long startTime = (new Date()).getTime();

		//Now we have to find which method can be used
		//We do a little bit at tricking by asking the pluginManager about the list of plug-ins
		//that can be run on this parameters.
		//If the plug-in name is not there it means that a problem occurred 
		//and we will throw an exception

		Set<PluginParameterBinding> listPlugins = null;

		if (args != null) {
			Class<?>[] argTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
				//System.out.println(argTypes[i].toString());
			}
			listPlugins = parent.getPluginManager().getPluginsAcceptingInAnyOrder(parent.getPluginContextType(), false,
					argTypes);
		} else {
			listPlugins = parent.getPluginManager().getPluginsAcceptingInAnyOrder(parent.getPluginContextType(), false);
		}

		PluginParameterBinding plugin = null;

		//System.out.println("Number of plugins = "+listPlugins.size());

		for (PluginParameterBinding p : listPlugins) {
			if (p.getPlugin().getName().equals(pluginName)) {
				plugin = p;
			}
			//System.out.println("Plugin: "+p.getPlugin().getName());
		}

		if (plugin == null) {
			//System.out.println("The plugin requested cannot be executed on the given parameters");
			throw new IllegalArgumentException("The plugin requested cannot be executed on the given parameters");
		}

		//List<Class<?>> returnType = plugin.getPlugin().getReturnTypes();

		//for (Class<?> retType:returnType){
		//System.out.println(retType.getCanonicalName());
		//}

		PluginExecutionResult result;

		if (args == null) {
			//System.out.println("Invoking the plugin");
			result = plugin.invoke(childContext);

		} else {
			//System.out.println("Invoking the plugin with arguments" + args.toString());
			result = plugin.invoke(childContext, args);
		}

		//System.out.println("I am waiting the plugin to finish");
		//while (!(listener.isCompleted()|| listener.isCancelled())){}

		//Object[] returnObjects = childContext.getResults();

		//System.out.println("That's it with the plugin");

		result.synchronize();

		Map<Class<?>, ProvidedObjectID> poid = new HashMap<Class<?>, ProvidedObjectID>();

		for (int j = 0; j < plugin.getPlugin().getReturnNames().size(); j++) {

			poid.put(plugin.getPlugin().getReturnTypes().get(j), parent.getProvidedObjectManager()
					.createProvidedObject(result.getResultName(j), result.getResult(j), childContext));
		}

		//System.out.println("That's it with the plugin");
		double executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin(pluginName, executionTime);

		if (!listener.isCancelled()) {
			return poid;
		} else {
			throw new Exception("Plugin Failed");
		}

	}

	/*
	 * public void callAPlugin(String pluginname) throws Exception{
	 * Map<Class<?>, ProvidedObjectID> poid =
	 * runPlugin(globalContext.getMainPluginContext(),pluginname, null);
	 * 
	 * Collection<ProvidedObjectID> colPO =
	 * globalContext.getProvidedObjectManager().getProvidedObjects();
	 * for(ProvidedObjectID object: colPO){
	 * System.out.println("My provided object with id="+object.toString()); }
	 * 
	 * Object[] po = new Object[poid.size()];
	 * 
	 * ProvidedObjectID[] poids = (ProvidedObjectID[]) poid.keySet().toArray();
	 * 
	 * for(int i=0; i<poid.size(); i++){ po[i] =
	 * globalContext.getProvidedObjectManager
	 * ().getProvidedObjectObject(poids[i],true);
	 * System.out.println(po[i].getClass().toString()); }
	 * //System.out.println("Run second plugin"); // poid =
	 * runPlugin(globalContext
	 * .getMainPluginContext(),"Elementary net Semantics Provider", po); }
	 */
	//	protected String serialize(Object o) {
	//		long startTime = System.currentTimeMillis();
	//		XStream xs = new XStream();
	//		String xml = xs.toXML(o);
	//		GridMiddlewareLogs.logTransfer("SerializeObject",o.getClass().getCanonicalName(), xml.length(), (System.currentTimeMillis()-startTime)/1000);
	//		return xml;
	//	}
	//
	//	protected Object deserialize(String xml) {
	//		long startTime = System.currentTimeMillis();
	//		XStream xs = new XStream();
	//		Object o = xs.fromXML(xml);
	//		GridMiddlewareLogs.logTransfer("DeserializeObject",o.getClass().getCanonicalName(), xml.length(), (System.currentTimeMillis()-startTime)/1000);
	//		return o;
	//		
	//	}
	/**
	 * Takes the input stream and creates an object from it. It adds the object
	 * to the framework and returns the provided object id
	 * 
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected ProvidedObjectID receiveObject(BufferedReader in) throws IOException {

		//System.out.println("Server receiving data");

		long startTime = (new Date()).getTime();
		String obj = TransferHelpFunctions.receiveXML(in);
		double executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin("ReceiveObject-Transfer", executionTime);
		//System.out.println("Client:" +obj);
		startTime = (new Date()).getTime();
		Object o = TransferHelpFunctions.deserialize(obj);
		executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin("ReceiveObject-Deserialize", executionTime);
		return globalContext.getProvidedObjectManager().createProvidedObject("Received object of type " + o.getClass(),
				o, globalContext);

	}

	protected ProvidedObjectID receiveLog(BufferedReader in) throws Exception {

		String filename = "Log received";
		long startTime = (new Date()).getTime();

		String obj = TransferHelpFunctions.receiveXML(in);

		double executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin("ReceiveLog-Transfer", executionTime);
		XLog log = (XLog) TransferHelpFunctions.deserializeLog(obj, filename);
		if (log == null) {
			return null;
		}
		return globalContext.getProvidedObjectManager().createProvidedObject(
				"Received object of type " + log.getClass(), log, globalContext);
	}

	/**
	 * Takes the input stream and creates an object from it. It adds the object
	 * to the framework and returns the provided object id
	 * 
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	protected ProvidedObjectID receiveRemoteObject(BufferedReader in) throws Exception {

		long startTime = (new Date()).getTime();
		//		while (!(line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(Protocol.FINISHED_OBJECT)) {
		//			obj += line;
		//		}
		String obj = TransferHelpFunctions.receiveXML(in);
		//System.out.println("Client:" +obj);

		RemoteProvidedObject o = (RemoteProvidedObject) TransferHelpFunctions.deserialize(obj);

		//this object has to be grabbed from another ProM instance
		//first I need to create a ProMClient

		ProMClient client = new ProMClient(o.getHost(), o.getPort());

		Object obj2;

		obj2 = client.receiveObject(o.getProvidedObjectID(), o.isLog());

		double executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin("ReceiveRemoteObject", executionTime);

		return globalContext.getProvidedObjectManager().createProvidedObject(
				"Received object of type " + obj2.getClass(), obj2, globalContext);

	}

	/**
	 * In this function we send a given object on a stream.
	 * 
	 * @param out
	 * @param o2
	 * @throws Exception
	 */
	protected void sendObject(PrintWriter out, ProvidedObjectID o2) throws Exception {

		// First we have to check if this exists
		/*
		 * boolean exists = false; for (ProvidedObjectID i:poid){
		 * System.out.println(i.toString()); if (i.equals(o2)) { poID = i;
		 * exists = true; break; } }
		 */

		//if (exists) {
		//First we find the object then we serialize it then we just send it
		//System.out.println("Server: the data exists");
		TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_REQUEST_RECEIVED);

		//I have to see how I deal with the ProM Futures
		long startTime = (new Date()).getTime();

		Object o = globalContext.getProvidedObjectManager().getProvidedObjectObject(o2, true);
		if (o != null) {
			if (o instanceof XLog) {

				XMxmlSerializer serializer = new XMxmlSerializer();

				OutputStream output = new ByteArrayOutputStream();

				serializer.serialize((XLog) o, output);

				//here I send the serialized object
				TransferHelpFunctions.writeXML(out, output.toString());

			} else {
				TransferHelpFunctions.writeXML(out, TransferHelpFunctions.serialize(o));
			}
		} else {
			//System.out.println("Server: the data doesn't exist");
			TransferHelpFunctions.writeLine(out, ProMProtocol.SERVER_ERROR);
		}

		double executionTime = (((new Date()).getTime() - startTime) / 1000.0);

		GridMiddlewareLogs.logPlugin("SendObject" + o.getClass(), executionTime);

	}

}
