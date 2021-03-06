package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Inventory;
import com.vast.component.Speed;

public class EncumbranceSystem extends IteratingSystem {
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Inventory> inventoryMapper;

	private float ratioWhenEncumbered;

	public EncumbranceSystem(int percentWhenEncumbered) {
		super(Aspect.all(Speed.class, Inventory.class));

		ratioWhenEncumbered = percentWhenEncumbered / 100f;
	}

	@Override
	protected void process(int entity) {
		Speed speed = speedMapper.get(entity);
		Inventory inventory = inventoryMapper.get(entity);

		if ((float) inventory.getNumberOfItems() / inventory.capacity >= ratioWhenEncumbered) {
			speed.modifier = 0.85f;
		} else {
			speed.modifier = 1.0f;
		}
	}
}
