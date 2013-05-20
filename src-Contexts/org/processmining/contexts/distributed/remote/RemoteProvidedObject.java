package org.processmining.contexts.distributed.remote;

import org.processmining.framework.providedobjects.ProvidedObjectID;

public class RemoteProvidedObject {

	private String host;
	private int port;
	private ProvidedObjectID pID;
	private final String type;

	public RemoteProvidedObject(String host, ProvidedObjectID pid, int port, String type) {
		super();
		this.host = host;
		pID = pid;
		this.port = port;
		this.type = type;
	}

	public ProvidedObjectID getProvidedObjectID() {
		return pID;
	}

	public void setProvidedObjectID(ProvidedObjectID pid) {
		pID = pid;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLog() {
		return type.equalsIgnoreCase("XLog");
	}

}
