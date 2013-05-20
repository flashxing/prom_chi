package org.processmining.contexts.distributed.remote.next;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class TransferHelpFunctions {

	public static synchronized String serializeLog(XLog o) throws IOException {

		XesXmlSerializer serializer = new XesXmlSerializer();

		OutputStream output = new ByteArrayOutputStream();

		serializer.serialize(o, output);

		String xml = output.toString();
		return xml;
	}

	public static Object deserializeLog(String xml, String id1) throws Exception, SAXException, IOException {
		// TODO Auto-generated method stub

		Object toReturn = null;
		byte[] bArray = xml.getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(bArray);

		Collection<XLog> logs = (new XesXmlParser()).parse(new XMonitoredInputStream(bais, bArray.length,
				new XProgressListener() {

					public boolean isAborted() {
						// TODO Auto-generated method stub
						return false;
					}

					public void updateProgress(int arg0, int arg1) {
						// TODO Auto-generated method stub

					}

				}));

		if (logs.size() == 0) {
			writeToFile(xml, "Error.txt");
			return null;
		} else {
			toReturn = logs.iterator().next();
			XConceptExtension.instance().assignName((XAttributable) toReturn, id1);
		}

		return toReturn;
	}

	//	public static void writeXML(PrintWriter out, String xml) {
	//		long startTime = System.currentTimeMillis();
	//		writeLine(out, xml);
	//		writeLine(out, Protocol.FINISHED_OBJECT);
	//		GridMiddlewareLogs.logTransfer("SentXML", "unknown", xml.length(),
	//				(System.currentTimeMillis() - startTime) / 1000);
	//	}
	//
	//	public static String receiveXML(BufferedReader in) throws IOException {
	//		String line;
	//		String obj = "";
	//		long startTime = System.currentTimeMillis();
	//		while (!(line = in.readLine()).equalsIgnoreCase(Protocol.FINISHED_OBJECT)) {
	//			obj += line;
	//		}
	//		GridMiddlewareLogs.logTransfer("ReceiveXML", "unknown", obj.length(),
	//				(System.currentTimeMillis() - startTime) / 1000);
	//		return obj;	
	//	}

	public static void writeXML(DataOutputStream out, String xml) {

		byte[] byteArray = xml.getBytes();
		try {
			//first I tell how many bytes to be read
			int length = byteArray.length;
			out.writeInt(length);
			//now I create a stream
			out.flush();
			out.write(byteArray);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String receiveXML(DataInputStream in) {

		//int id = (new Date()).hashCode();
		//writeToFile("rcvXML , "+id,"Input.csv");
		try {
			int lengthToRead = in.readInt();
			//int bytesRead = 0;
			byte[] byteRead = new byte[lengthToRead];
			in.readFully(byteRead);
			String s = new String(byteRead);//).substring(0, lengthToRead);
			return (s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public static synchronized void writeToFile(String msg, String filename) {
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));

			//writer.write("PopulationNumber, Mean, Variance, StandardDeviation, BestFitness \n");

			//for (int i = 0; i < settings.getMaxGeneration(); i++) {
			writer.write(System.currentTimeMillis() + " , " + msg + "\r\n");
			//}

			//			writer.write("Elapsed Time " + elapsedTime + "\n");
			//
			//			writer.write(settings.toString() + "\n");
			//
			//			writer.write((new Date()).toString() + "\n");

			writer.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String serialize(Object o) {

		//		XStream xstream = new XStream();
		//		XesXStreamPersistency.register(xstream);
		//		ObjectOutputStream out = xstream.createObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(
		//				new FileOutputStream(f))));
		//		out.writeObject(o);
		//		out.close();

		XStream xs = new XStream();
		String xml = xs.toXML(o);
		return xml;
	}

	public static Object deserialize(String xml) {
		XStream xs = new XStream();
		Object o = xs.fromXML(xml);
		if (o == null) {
			writeToFile(xml, "Error.txt");
		}
		return o;
	}

	/**
	 * Writes to the output buffer
	 */
	public static void writeLine(DataOutputStream out, String line) {
		try {
			out.writeUTF(line);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Writes to the output buffer
	 */
	public static String readLine(DataInputStream in) {
		String line;
		try {
			line = DataInputStream.readUTF(in);
			return line;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void serialize(Object o, Writer out) {
		// TODO Auto-generated method stub
		XStream xs = new XStream();
		xs.toXML(o, out);
	}

}