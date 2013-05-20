package org.processmining.contexts.distributed;

public class GridMiddlewareLogs {

	public static String jobLogs;

	public static String resLogs;

	public synchronized static void initialize() {
		//		try {
		//			jobLogs = "/home/cbratosi/GMLogs/" + "plugins-"
		//					+ java.net.InetAddress.getLocalHost().getCanonicalHostName() + "-" + Distributed.port
		//					+ (new Date()).hashCode() + ".csv";
		//						resLogs = "/home/cbratosi/GMLogs/" + "resource-"
		//								+ java.net.InetAddress.getLocalHost().getCanonicalHostName() + "-" + Distributed.port
		//								+ (new Date()).hashCode() + ".csv";
		//		} catch (UnknownHostException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}
		//
		//		BufferedWriter writer;
		//		try {
		//			writer = new BufferedWriter(new FileWriter(jobLogs, true));
		//			writer.write("PluginName, ExecutionTime, TimeStamp");
		//			writer.write('\n');
		//			writer.close();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}

	public synchronized static void logPlugin(String name, double time) {
		//		BufferedWriter writer;
		//		try {
		//			writer = new BufferedWriter(new FileWriter(jobLogs, true));
		//			writer.write(name + ", " + time + ", " + (new Date()).toString());
		//			writer.write('\n');
		//			writer.close();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}

	public synchronized static void logTransfer(String name, String objectType, long length, double time) {
		//		BufferedWriter writer;
		//		try {
		//			writer = new BufferedWriter(new FileWriter(jobLogs, true));
		//			writer.write(name + ", " + objectType + " , "+length+" , "+ time + ", " + (new Date()).toString());
		//			writer.write('\n');
		//			writer.close();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

	}

	public synchronized static void logResource(String xml) {
		//				BufferedWriter writer;
		//				try {
		//					writer = new BufferedWriter(new FileWriter(resLogs, true));
		//					writer.write(xml + "\n");
		//					writer.close();
		//				} catch (IOException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}
		//
		//	}
	}
}
