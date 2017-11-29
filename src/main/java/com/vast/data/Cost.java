package com.vast.data;

public class Cost {
	private int itemType;
	private int count;

	public Cost() {
		itemType = -1;
		count = 0;
	}

	public Cost(int itemType, int count) {
		this.itemType = itemType;
		this.count = count;
	}

	public int getItemType() {
		return itemType;
	}

	public int getCount() {
		return count;
	}
}
