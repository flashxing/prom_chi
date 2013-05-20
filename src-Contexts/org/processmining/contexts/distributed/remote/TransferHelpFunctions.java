package org.processmining.contexts.distributed.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.util.progress.XMonitoredInputStream;
import org.deckfour.xes.util.progress.XProgressListener;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class TransferHelpFunctions {

	public static String serializeLog(XLog o) throws IOException {

		XMxmlSerializer serializer = new XMxmlSerializer();

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

		Collection<XLog> logs = (new XMxmlParser()).parse(new XMonitoredInputStream(bais, xml.getBytes().length,
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
			return null;
		} else {
			toReturn = logs.iterator().next();
			XConceptExtension.instance().assignName((XAttributable) toReturn, id1);
		}

		return toReturn;
	}

	public static void writeXML(PrintWriter out, String xml) {
		writeLine(out, xml);
		writeLine(out, ProMProtocol.FINISHED_OBJECT);
	}

	public static String receiveXML(BufferedReader in) throws IOException {
		String line;
		String obj = "";
		while (!(line = in.readLine()).equalsIgnoreCase(ProMProtocol.FINISHED_OBJECT)) {
			obj += line;
		}
		return obj;
	}

	public static String serialize(Object o) {
		XStream xs = new XStream();
		String xml = xs.toXML(o);
		return xml;
	}

	public static Object deserialize(String xml) {
		XStream xs = new XStream();
		Object o = xs.fromXML(xml);
		return o;
	}

	/**
	 * Writes to the output buffer
	 */
	public static void writeLine(PrintWriter out, String line) {
		out.write(line + "\r\n");
		out.flush();
	}

	public static String readLine(BufferedReader in) {
		// TODO Auto-generated method stub
		try {
			return in.readLine();
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