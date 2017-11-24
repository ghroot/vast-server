package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.data.Cost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

public class Inventory extends PooledComponent {
	private static final Logger logger = LoggerFactory.getLogger(Inventory.class);

	public short[] items = new short[0];

	@Override
	protected void reset() {
		items = new short[0];
	}

	public void add(int itemType, int amount) {
		logger.debug("Adding {} item(s) of type {} to inventory", amount, itemType);
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

	public void remove(int itemType, int amount) {
		logger.debug("Removing {} item(s) of type {} from inventory", amount, itemType);
		items[itemType] -= amount;
	}

	public void remove(Set<Cost> costs) {
		for (Cost cost : costs) {
			remove(cost.getItem().getType(), cost.getCount());
		}
	}

	public boolean has(int itemType, int amount) {
		return itemType < items.length && items[itemType] >= amount;
	}

	public boolean has(Set<Cost> costs) {
		for (Cost cost : costs) {
			if (!has(cost.getItem().getType(), cost.getCount())) {
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
