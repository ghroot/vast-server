package com.vast.data;

public class Item {
	private int id;
	private String type;
	private String name;

	public Item(int id, String type, String name) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
