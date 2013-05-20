package org.processmining.contexts.distributed.remote.next.prom;

import java.io.IOException;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.distributed.remote.ProMProtocol;
import org.processmining.contexts.distributed.remote.RemoteProvidedObject;
import org.processmining.contexts.distributed.remote.next.AbstractClient;
import org.processmining.contexts.distributed.remote.next.TransferHelpFunctions;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class ProMClient extends AbstractClient {

	public ProMClient(String Host, int Port) {
		super(Host, Port);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * The method sends a message over the network
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws Exception
	 */
	public void sendMessage(String msg) throws IOException, Exception {
		openSocket();
		initiateCommunication();
		TransferHelpFunctions.writeLine(out, msg);
		String line;
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}
		endCommunication();
		closeSocket();

	}

	/**
	 * 
	 * The method sends an object over the network
	 * 
	 * @param o
	 *            the Object that needs to be sent
	 * @throws IOException
	 * @throws Exception
	 */
	public ProvidedObjectID sendObject(Object o) throws IOException, Exception {
		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_SEND_DATA_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}

		String xml = TransferHelpFunctions.serialize(o);

		//here I send the serialized object
		TransferHelpFunctions.writeXML(out, xml);

		//here I receive PO ID
		line = TransferHelpFunctions.receiveXML(in);
		endCommunication();
		closeSocket();

		return (ProvidedObjectID) (TransferHelpFunctions.deserialize(line));

	}

	public ProvidedObjectID sendLog(XLog o) throws IOException, Exception {
		openSocket();
		initiateCommunication();
		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_SEND_LOG_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}

		String xml = TransferHelpFunctions.serializeLog(o);

		//here I send the serialized object
		TransferHelpFunctions.writeXML(out, xml);

		//writeLine(out,Protocol.CLIENT_FINISHED_OBJECT);

		//here I receive PO ID
		line = TransferHelpFunctions.receiveXML(in);

		endCommunication();
		closeSocket();

		return (ProvidedObjectID) (TransferHelpFunctions.deserialize(line));

	}

	public ProvidedObjectID sendLog(String xml) throws IOException, Exception {
		openSocket();
		initiateCommunication();
		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_SEND_LOG_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}

		//here I send the serialized object
		TransferHelpFunctions.writeXML(out, xml);

		//writeLine(out,Protocol.CLIENT_FINISHED_OBJECT);

		//here I receive PO ID
		line = TransferHelpFunctions.receiveXML(in);

		endCommunication();
		closeSocket();

		return (ProvidedObjectID) (TransferHelpFunctions.deserialize(line));

	}

	/**
	 * 
	 * The method sends a remote object over the network
	 * 
	 * the idea is that ProM will try to get this object from another ProM :)
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws Exception
	 */
	public ProvidedObjectID sendRemoteObject(String host, int port, ProvidedObjectID poID, String type)
			throws IOException, Exception {
		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_SEND_REMOTE_DATA_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}

		Object o = new RemoteProvidedObject(host, poID, port, type);

		String xml = TransferHelpFunctions.serialize(o);
		TransferHelpFunctions.writeXML(out, xml);
		line = TransferHelpFunctions.receiveXML(in);
		endCommunication();
		closeSocket();

		return (ProvidedObjectID) (TransferHelpFunctions.deserialize(line));

	}

	/**
	 * 
	 * The method receives an object over the network
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("cast")
	public Object receiveObject(ProvidedObjectID id1, boolean isLog) throws IOException, Exception {
		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_RECEIVE_DATA_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong

				throw new Exception("Something went wrong on server side");
			}
		}

		//System.out.println("Server:" +line);

		//here I send the serialized PO ID
		String xml = TransferHelpFunctions.serialize(id1);
		TransferHelpFunctions.writeXML(out, xml);

		boolean error = false;
		//now I have to see what the server says 
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong
				error = true;
				throw new Exception("Something went wrong on server side");
			}
		}

		if (!error) {
			//everything is ok so now I have to receive the object
			xml = TransferHelpFunctions.receiveXML(in);
		}
		Object toReturn = null;
		if (isLog) {
			toReturn = (Object) (TransferHelpFunctions.deserializeLog(xml, id1.toString()));
		} else {
			toReturn = (Object) (TransferHelpFunctions.deserialize(xml));
		}

		endCommunication();
		closeSocket();
		if (error) {
			throw new Exception("Something went wrong at the server side");
		}
		return toReturn;
	}

	/**
	 * 
	 * The method sends an object over the network
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<Class<?>, ProvidedObjectID> runPlugin(String pluginName, Object[] args) throws IOException, Exception {

		Object[] argsDuplicate = null;

		if (args != null) {
			argsDuplicate = new Object[args.length];
			//here I send the parameters; they can be provided object or a real object
			int i = 0;
			for (Object o : args) {
				//Class<?> clazz = o.getClass();
				//System.out.println("It is a log: "+ XLog.class.isInstance(o) + " class is ... " + clazz.getName());
				if (XLog.class.isInstance(o)) {
					//TransferHelpFunctions.writeXML(out, TransferHelpFunctions.serializeLog((XLog)o));
					System.out.println("Sending a looooooooooooog");
					ProvidedObjectID id = this.sendLog((XLog) o);
					argsDuplicate[i++] = id;
				} else {
					argsDuplicate[i++] = o;
				}
			}
		}

		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_RUN_PLUGIN_REQUEST);
		String line = "";
		while (line.equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			line = TransferHelpFunctions.readLine(in);
		}

		//System.out.println(line);
		//here I send the name of the plugin
		TransferHelpFunctions.writeLine(out, pluginName);

		boolean error = false;

		//now I wait for server response
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_SEND_PARAMETERS) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong
				error = true;
				throw new Exception("Something went wrong on server side");
			}
		}

		//HERE I HAVE TO SEND THE PARAMETERS
		if (argsDuplicate != null) {
			//here I send the parameters; they can be provided object or a real object
			for (Object o : argsDuplicate) {
				TransferHelpFunctions.writeXML(out, TransferHelpFunctions.serialize(o));
			}
		}

		//I announce the server that I finished sending the parameters
		TransferHelpFunctions.writeXML(out, ProMProtocol.CLIENT_FINISHED_PARAMETERS);

		//I wait for the provided object ids
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_SEND_PO_IDS) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				//something went wrong
				error = true;
				throw new Exception("Something went wrong on server side");
			}
		}
		Map<Class<?>, ProvidedObjectID> poids = null;
		if (!error) {
			line = TransferHelpFunctions.receiveXML(in);
			poids = (Map<Class<?>, ProvidedObjectID>) TransferHelpFunctions.deserialize(line);

		}

		endCommunication();
		closeSocket();
		if (!error) {
			return poids;
		} else {
			throw new Exception("Something went wrong on server side");
		}

	}

	public void discardData(ProvidedObjectID id) throws IOException, Exception {
		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_DISCARD_DATA_REQUEST);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
		}
		//here I send the serialized PO ID
		String xml = TransferHelpFunctions.serialize(id);

		TransferHelpFunctions.writeXML(out, xml);

		//now I have to see what the server says 
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_REQUEST_RECEIVED) == false) {
			if (line.equalsIgnoreCase(ProMProtocol.SERVER_ERROR)) {
				throw new Exception("Something went wrong on server side");
			}
		}

	}
	//	public static void main(String[] args) {
	//	
	//	XLog log = new XLogImpl(null); 
	//	
	//	Class<?> clazz = log.getClass();
	//	
	//	System.out.println("It is a log: "+ XLog.class.isInstance(log) + " class is ... " + XLog.class.isAssignableFrom(clazz));
	//}

}
