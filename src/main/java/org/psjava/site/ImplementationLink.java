package org.psjava.site;

public class ImplementationLink {
	public final String simpleClassName;
	public final String url;

	public ImplementationLink(String simpleClassName, String url) {
		this.simpleClassName = simpleClassName;
		this.url = url;
	}

	public String getSimpleClassName() {
		return simpleClassName;
	}

	public String getUrl() {
		return url;
	}
}
