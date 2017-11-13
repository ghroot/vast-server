package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.system.CreationManager;
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

	public void remove(short type, int amount) {
		logger.debug("Removing {} item(s) of type {} from inventory", amount, type);
		items[type] -= amount;
	}
}
