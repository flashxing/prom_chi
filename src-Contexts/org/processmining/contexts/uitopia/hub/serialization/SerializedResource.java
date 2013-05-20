package org.processmining.contexts.uitopia.hub.serialization;

import java.util.HashSet;
import java.util.Set;

import org.processmining.contexts.uitopia.model.ProMResource;
import org.processmining.framework.ProMID;

public class SerializedResource {

	public static class Update {
		public ProMID id;
		public String name;
		public boolean favorite;

		Update(ProMResource<?> resource) {
			name = resource.getName();
			id = resource.getID();
			favorite = resource.isFavorite();
		}
	}

	public Class<?> type;
	public Object instance;
	public String name;
	public boolean favorite;
	public Set<ProMID> parents;
	public long creationTime;
	public ProMID id;

	public SerializedResource() {

	}

	public SerializedResource(ProMResource<?> resource) {

		name = resource.getName();
		id = resource.getID();

		instance = resource.getInstance();
		favorite = resource.isFavorite();
		type = resource.getType().getTypeClass();
		parents = new HashSet<ProMID>();
		creationTime = resource.getCreationTime().getTime();
		for (ProMResource<?> parent : resource.getParents()) {
			parents.add(parent.getID());
		}
	}

}
