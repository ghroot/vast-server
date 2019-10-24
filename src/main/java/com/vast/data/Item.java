package com.vast.data;

public class Item {
	private int id;
	private String[] tags;
	private String name;

	public Item(int id, String[] tags, String name) {
		this.id = id;
		this.tags = tags;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String[] getTags() {
		return tags;
	}

	public boolean hasTag(String tag) {
		for (String aTag : tags) {
			if (aTag.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	public String getName() {
		return name;
	}
}
