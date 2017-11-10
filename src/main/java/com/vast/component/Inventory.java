package com.vast.component;

import com.artemis.PooledComponent;

import java.util.Arrays;

public class Inventory extends PooledComponent {
	public short[] items = new short[0];

	@Override
	protected void reset() {
		items = new short[0];
	}

	public void add(int type, int amount) {
		if (type >= items.length) {
			items = Arrays.copyOf(items, type + 1);
		}
		items[type] += amount;
	}

	public void remove(short type, int amount) {
		items[type] -= amount;
	}
}
