package com.vast.data;

public class Item {
	private int type;
	private String name;

	public Item(int type, String name) {
		this.type = type;
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
