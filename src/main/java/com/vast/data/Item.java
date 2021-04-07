package com.vast.data;

import java.util.Set;

public class Item {
	private int id;
	private Set<String> tags;
	private String name;

	public Item(int id, Set<String> tags, String name) {
		this.id = id;
		this.tags = tags;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}

	public String getName() {
		return name;
	}
}
