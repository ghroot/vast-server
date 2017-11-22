package com.vast.component;

import com.artemis.PooledComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Inventory extends PooledComponent {
	private static final Logger logger = LoggerFactory.getLogger(Inventory.class);

	public short[] items = new short[0];

	@Override
	protected void reset() {
		items = new short[0];
	}

	public void add(int type, int amount) {
		logger.debug("Adding {} item(s) of type {} to inventory", amount, type);
		if (type >= items.length) {
			items = Arrays.copyOf(items, type + 1);
		}
		items[type] += amount;
	}

	public void add(short[] otherItems) {
		for (int type = 0; type < otherItems.length; type++) {
			if (otherItems[type] > 0) {
				add(type, otherItems[type]);
			}
		}
	}

	public void remove(int type, int amount) {
		logger.debug("Removing {} item(s) of type {} from inventory", amount, type);
		items[type] -= amount;
	}

	public boolean has(int type, int amount) {
		return type < items.length && items[type] >= amount;
	}

	public void clear() {
		items = new short[0];
	}
}
