package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.data.Cost;

import java.util.Arrays;
import java.util.Set;

public class Inventory extends PooledComponent {
	public short[] items = new short[0];

	@Override
	protected void reset() {
		items = new short[0];
	}

	public void add(int itemType, int amount) {
		if (itemType >= items.length) {
			items = Arrays.copyOf(items, itemType + 1);
		}
		items[itemType] += amount;
	}

	public void add(short[] otherItems) {
		for (int type = 0; type < otherItems.length; type++) {
			if (otherItems[type] > 0) {
				add(type, otherItems[type]);
			}
		}
	}

	public void add(Inventory otherInventory) {
		add(otherInventory.items);
	}

	public void remove(int itemType, int amount) {
		if (amount > 0) {
			items[itemType] -= amount;
		}
	}

	public void remove(Cost cost) {
		remove(cost.getItemType(), cost.getCount());
	}

	public void remove(Set<Cost> costs) {
		for (Cost cost : costs) {
			remove(cost);
		}
	}

	public boolean has(int itemType, int amount) {
		if (amount > 0) {
			return itemType < items.length && items[itemType] >= amount;
		} else {
			return true;
		}
	}

	public boolean has(Cost cost) {
		return has(cost.getItemType(), cost.getCount());
	}

	public boolean has(Set<Cost> costs) {
		for (Cost cost : costs) {
			if (!has(cost.getItemType(), cost.getCount())) {
				return false;
			}
		}
		return true;
	}

	public void clear() {
		items = new short[0];
	}

	public boolean isEmpty() {
		for (int type = 0; type < items.length; type++) {
			if (items[type] > 0) {
				return false;
			}
		}
		return true;
	}
}
