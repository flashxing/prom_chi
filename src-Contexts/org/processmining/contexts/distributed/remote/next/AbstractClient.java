package org.processmining.contexts.distributed.remote.next;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.processmining.contexts.distributed.remote.ProMProtocol;

public class AbstractClient {

	protected String host;
	protected int port;
	protected Socket socket = null;
	protected DataOutputStream out = null;
	protected DataInputStream in = null;

	public AbstractClient(String Host, int Port) {
		super();
		host = Host;
		port = Port;
	}

	/**
	 * Open the communication
	 */
	protected void openSocket() throws Exception, IOException {
		//System.out.println("Open Connection");
		socket = new Socket(host, port);
		//ProMSocket.setKeepAlive(true);
		//ProMSocket.setSoTimeout(0);

		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * Close the communication
	 */
	protected void closeSocket() throws IOException {
		//System.out.println("Close Connection");
		out.close();
		in.close();
		socket.close();
		socket = null;
		in = null;
		out = null;
	}

	/**
	 * Retrieves the port number on which communication was established
	 * 
	 * @return port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Modifies the port number on which communication takes place
	 * 
	 * @param Port
	 *            new port number
	 */
	public void setPort(int Port) {
		port = Port;
	}

	/**
	 * Retrieves the host identification on which communication was established
	 * 
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Modifies the host identification on which communication was established
	 * 
	 * @param Host
	 *            new host
	 */
	public void setHost(String Host) {
		host = Host;
	}

	/**
	 * Initiate the communication between the client and the server by sending
	 * Hello greetings
	 */
	protected void initiateCommunication() throws Exception {
		String line = TransferHelpFunctions.readLine(in);
		//System.out.println("Server:" + line);
		if (line.equals(ProMProtocol.HELO_SERVER) == false) {
			throw new Exception("Something is wrong here!!!");
		}
	}

	/**
	 * Sends the end communication protocol
	 */
	protected void endCommunication() {
		// TODO Auto-generated method stub
		TransferHelpFunctions.writeLine(out, ProMProtocol.CLIENT_GOODBYE);
		@SuppressWarnings("unused")
		String line = null;
		while ((line = TransferHelpFunctions.readLine(in)).equalsIgnoreCase(ProMProtocol.SERVER_GOODBYE) == false) {
		}
		//System.out.println("Server:" +line);
	}

}