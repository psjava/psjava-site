package org.psjava.site;

public class SeeAlsoLink {
	public final String name;
	public final String category;
	public final String id;

	public SeeAlsoLink(String name, String category, String id) {
		this.name = name;
		this.category = category;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getId() {
		return id;
	}
}
