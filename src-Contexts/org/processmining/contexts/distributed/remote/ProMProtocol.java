package org.processmining.contexts.distributed.remote;

public class ProMProtocol {

	public static final String HELO_SERVER = "I am listening ...";
	public static final String SERVER_REQUEST_RECEIVED = "I have received your request ...";
	public static final String CLIENT_GOODBYE = "Bye, Bye dear Server !!!";
	public static final String SERVER_GOODBYE = "Bye, Bye dear Client !!!";
	public static final String CLIENT_REQUEST = "Can you do this for me?";
	public static final String CLIENT_SEND_DATA_REQUEST = "I want to send some data";
	public static final String CLIENT_RECEIVE_DATA_REQUEST = "I want to receive some data";
	public static final String CLIENT_RUN_PLUGIN_REQUEST = "I want to run a plugin";

	public static final String SERVER_ERROR = "I can't fullfil the request";
	public static final String SERVER_SEND_PARAMETERS = "Send me the parameters for the plugin";
	public static final String CLIENT_FINISHED_PARAMETERS = "That was it with the parameters!";
	public static final String SERVER_SEND_PO_IDS = "I am sending the ids of the resulted objects";
	public static final String SERVER_FINISHED_PO_IDS = "That was it with the provided objects.";
	//public static final String CLIENT_FINISHED_OBJECT = "That was it with the object";
	public static final String FINISHED_OBJECT = "That was it with the object";
	public static final String CLIENT_SEND_REMOTE_DATA_REQUEST = "This is a remote data request..";
	public static final String CLIENT_DISCARD_DATA_REQUEST = "Get rid of this ... The police is coming :D";
	public static final String SERVER_DISCARDED_DATA = "That was it ... I discarded the data";
	public static final String CLIENT_ISLAND_END = "The island is done ...";
	public static String CLIENT_SEND_LOG_REQUEST = "I am sending a looooog";

}
