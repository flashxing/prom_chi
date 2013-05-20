package org.processmining.contexts.uitopia.model;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.uitopia.api.model.Author;
import org.deckfour.xes.model.XLog;

public class ProMResourceTypeInformation {

	private final static String TUE = "Eindhoven University of Technology";
	private final static String PMO = "http://www.processmining.org";

	private final static Map<Class<?>, ResourceTypeInfo> typeInfo = new HashMap<Class<?>, ResourceTypeInfo>();
	private static ProMResourceTypeInformation instance;

	private ProMResourceTypeInformation() {
		// XLog.class
		typeInfo.put(XLog.class, new ResourceTypeInfo("Event Log", TUE, "h.m.w.verbeek@tue.nl", "Eric Verbeek", PMO,
				"resourcetype_log_30x35.png"));

		// More classes
	}

	public final static ProMResourceTypeInformation getInstance() {
		if (instance == null) {
			instance = new ProMResourceTypeInformation();
		}
		return instance;
	}

	public ResourceTypeInfo getInfoFor(Class<?> type) {
		return typeInfo.get(type);
	}
}

class ResourceTypeInfo implements Author {

	public String affiliation;
	public String email;
	public String author;
	public String website;
	public String icon;
	public String typename;

	public ResourceTypeInfo(String typename, String affiliation, String email, String author, String website,
			String icon) {
		this.typename = typename;
		this.affiliation = affiliation;
		this.email = email;
		this.author = author;
		this.website = website;
		this.icon = icon;
	}

	public String getTypeName() {
		return typename;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return author;
	}

	public URI getWebsite() {
		URI uri = null;
		try {
			uri = new URL(website).toURI();
		} catch (Exception e2) {
		}
		return uri;
	}

	public String getIcon() {
		return icon;
	}

}
