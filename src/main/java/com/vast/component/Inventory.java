package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.data.Cost;
import com.vast.data.Item;

import java.util.Arrays;
import java.util.Set;

public class Inventory extends PooledComponent {
	public short[] items = new short[0];
	public int capacity = Integer.MAX_VALUE;

	@Override
	protected void reset() {
		items = new short[0];
		capacity = Integer.MAX_VALUE;
	}

	public void add(int itemId, int amount) {
		if (itemId >= items.length) {
			items = Arrays.copyOf(items, itemId + 1);
		}
		int amountToAdd = Math.min(amount, capacity - getNumberOfItems());
		items[itemId] += amountToAdd;
	}

	public void add(Item item, int amount) {
		add(item.getId(), amount);
	}

	public void add(short[] otherItems) {
		for (int itemId = 0; itemId < otherItems.length; itemId++) {
			if (otherItems[itemId] > 0) {
				add(itemId, otherItems[itemId]);
			}
		}
	}

	public void add(Inventory otherInventory) {
		add(otherInventory.items);
	}

	public void remove(int itemId, int amount) {
		if (amount > 0) {
			items[itemId] -= amount;
		}
	}

	public void remove(int itemId) {
		remove(itemId, 1);
	}

	public void remove(Item item) {
		remove(item.getId());
	}

	public void remove(Cost cost) {
		remove(cost.getItemId(), cost.getCount());
	}

	public void remove(Set<Cost> costs) {
		for (Cost cost : costs) {
			remove(cost);
		}
	}

	public boolean has(int itemId, int amount) {
		if (amount > 0) {
			return itemId < items.length && items[itemId] >= amount;
		} else {
			return true;
		}
	}

	public boolean has(int itemId) {
		return has(itemId, 1);
	}

	public boolean has(Item item) {
		return has(item.getId());
	}

	public boolean has(Cost cost) {
		return has(cost.getItemId(), cost.getCount());
	}

	public boolean has(Set<Cost> costs) {
		for (Cost cost : costs) {
			if (!has(cost.getItemId(), cost.getCount())) {
				return false;
			}
		}
		return true;
	}

	public void clear() {
		items = new short[0];
	}

	public boolean isEmpty() {
		for (int itemId = 0; itemId < items.length; itemId++) {
			if (items[itemId] > 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isFull() {
		return getNumberOfItems() >= capacity;
	}

	public int getNumberOfItems() {
		int numberOfItems = 0;
		for (int itemId = 0; itemId < items.length; itemId++) {
			numberOfItems += items[itemId];
		}
		return numberOfItems;
	}
}
