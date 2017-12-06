package com.vast.data;

public class Cost {
	private int itemId;
	private int count;

	public Cost() {
		itemId = -1;
		count = 0;
	}

	public Cost(int itemId, int count) {
		this.itemId = itemId;
		this.count = count;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}
}
