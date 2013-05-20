package org.processmining.contexts.uitopia.hub.serialization;

import java.util.Collection;

import org.processmining.contexts.uitopia.hub.serialization.ProMReferenceMarshaller.ReferenceListener;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ReusingReferenceByIdMarshallingStrategy implements MarshallingStrategy {

	private ProMReferenceMarshaller marshaller;
	private ProMUnMarshaller unmarshaller;
	private final ReferenceListener listener;
	private Collection<Object> referencesToKeep;

	public ReusingReferenceByIdMarshallingStrategy(ReferenceListener listener, Collection<Object> referencesToKeep) {
		this.listener = listener;
		this.referencesToKeep = referencesToKeep;

	}

	public synchronized void marshal(HierarchicalStreamWriter writer, Object obj, ConverterLookup converterLookup,
			Mapper mapper, DataHolder dataHolder) {
		if (marshaller == null) {
			marshaller = new ProMReferenceMarshaller(writer, converterLookup, mapper, listener);
		}
		synchronized (obj) {
			marshaller.start(obj, dataHolder);
		}
	}

	public synchronized Object unmarshal(Object root, HierarchicalStreamReader reader, DataHolder dataHolder,
			ConverterLookup converterLookup, Mapper mapper) {
		if (unmarshaller == null) {
			unmarshaller = new ProMUnMarshaller(root, reader, converterLookup, mapper, referencesToKeep);
		}
		return unmarshaller.start(dataHolder);

	}

}