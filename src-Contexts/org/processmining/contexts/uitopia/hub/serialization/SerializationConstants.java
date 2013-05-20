package org.processmining.contexts.uitopia.hub.serialization;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.deckfour.xes.xstream.XesXStreamPersistency;
import org.processmining.contexts.uitopia.hub.serialization.ProMReferenceMarshaller.ReferenceListener;
import org.processmining.framework.util.OsUtil;

import com.thoughtworks.xstream.XStream;

public class SerializationConstants {

	public final static String OBJECTPERSISTENCYFILE = "UITopiaResources.xml.zip";
	public final static String REMOVEDPERSISTENCYFILE = "UITopiaIndex.xml.zip";

	public static File getFile(String name) throws IOException {
		File f;

		f = new File(OsUtil.getProMWorkspaceDirectory(), name);
		if (!f.exists()) {
			f.createNewFile();
			return f;
		}
		return f;

	}

	public static XStream createXStream() {
		return createXStream(null, null);

	}

	public static XStream createXStream(ReferenceListener listener) {
		return createXStream(listener, null);
	}

	public static XStream createXStream(Collection<Object> referencesToKeep) {
		return createXStream(null, referencesToKeep);
	}

	public static XStream createXStream(ReferenceListener listener, Collection<Object> referencesToKeep) {
		XStream xstream = new XStream();
		xstream.setMarshallingStrategy(new ReusingReferenceByIdMarshallingStrategy(listener, referencesToKeep));
		xstream.autodetectAnnotations(true);
		XesXStreamPersistency.register(xstream);
		return xstream;
	}

}
