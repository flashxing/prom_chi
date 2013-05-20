package org.processmining.contexts.distributed.middleware.connect;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.processmining.contexts.distributed.Distributed;
import org.processmining.contexts.distributed.GridMiddlewareLogs;
import org.processmining.contexts.distributed.middleware.connect.description.DataElement;
import org.processmining.contexts.distributed.middleware.connect.description.DataList;
import org.processmining.contexts.distributed.middleware.connect.description.ResourceDescriptionType;
import org.processmining.contexts.distributed.remote.next.AbstractClient;
import org.processmining.contexts.distributed.remote.next.TransferHelpFunctions;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.providedobjects.ProvidedObjectID;

public class GMClient extends AbstractClient {

	/**
	 * Creates a new client instance
	 * 
	 * @param Host
	 *            IP address
	 * @param Port
	 *            port number *
	 */
	public GMClient(String Host, int Port) {
		super(Host, Port);
	}

	/**
	 * 
	 * The method sends an object over the network
	 * 
	 * @param msg
	 * @throws IOException
	 * @throws Exception
	 */
	public void sendInformation(PluginContext context) throws IOException, Exception {
		openSocket();
		initiateCommunication();

		TransferHelpFunctions.writeLine(out, GMProtocol.RESOURCE_INFO);

		String line;

		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(GMProtocol.SERVER_REQUEST_RECEIVED) == false) {
		}

		System.out.println("Server:" + line);

		ResourceDescriptionType resdescr = populateResDescr(context);
		//resdescr.setURI();

		String xml = TransferHelpFunctions.serialize(resdescr);

		//here I send the serialized object
		GridMiddlewareLogs.logResource(xml);
		TransferHelpFunctions.writeXML(out, xml);

		endCommunication();
		closeSocket();

	}

	/**
	 * @param context
	 * @return
	 */
	private ResourceDescriptionType populateResDescr(PluginContext context) {
		ResourceDescriptionType resdescr = new ResourceDescriptionType();
		resdescr.setOSProperties(Distributed.port);
		List<ProvidedObjectID> dataList = context.getProvidedObjectManager().getProvidedObjects();
		Iterator<ProvidedObjectID> it = dataList.iterator();
		DataList dl = new DataList();
		while (it.hasNext()) {
			ProvidedObjectID id = it.next();
			DataElement d = new DataElement();
			d.setPhysicalName(id.toString());
			d.setFileSize(getSize(context, id));
			dl.getDataList().add(d);
		}
		resdescr.setDataList(dl);
		return resdescr;
	}

	private double getSize(PluginContext context, ProvidedObjectID id) {
		// TODO Auto-generated method stub
		return 0;
	}

}
