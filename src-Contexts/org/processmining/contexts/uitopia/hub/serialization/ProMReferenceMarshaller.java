package org.processmining.contexts.uitopia.hub.serialization;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByIdMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.mapper.Mapper;

public class ProMReferenceMarshaller extends ReferenceByIdMarshaller {

	private final ReferenceListener listener;
	private long counter;

	public static interface ReferenceListener {
		public void referenceAdded(Object existingReferenceKey);

		public void keyCreated(Object existingReferenceKey);
	}

	public ProMReferenceMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper,
			ReferenceListener listener) {
		super(writer, converterLookup, mapper);
		this.listener = listener;
	}

	@Override
	protected String createReference(Path currentPath, Object existingReferenceKey) {
		if (listener != null) {
			listener.referenceAdded(existingReferenceKey);
		}
		return existingReferenceKey.toString();
	}

	@Override
	protected Object createReferenceKey(Path currentPath, Object item) {
		Long reference = counter++;
		if (listener != null) {
			listener.keyCreated(reference);
		}
		return reference;
	}

}
