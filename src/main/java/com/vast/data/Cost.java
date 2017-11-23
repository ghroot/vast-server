package com.vast.data;

public class Cost {
	private Item item;
	private int count;

	public Cost(Item item, int count) {
		this.item = item;
		this.count = count;
	}

	public Item getItem() {
		return item;
	}

	public int getCount() {
		return count;
	}
}
