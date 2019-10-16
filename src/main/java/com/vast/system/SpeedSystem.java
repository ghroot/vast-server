package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Inventory;
import com.vast.component.Speed;

public class SpeedSystem extends IteratingSystem {
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Inventory> inventoryMapper;

	public SpeedSystem() {
		super(Aspect.all(Speed.class, Inventory.class));
	}

	@Override
	protected void process(int entity) {
		Speed speed = speedMapper.get(entity);
		Inventory inventory = inventoryMapper.get(entity);

		if ((float) inventory.getNumberOfItems() / inventory.capacity >= 0.75f) {
			speed.modifier = 0.85f;
		} else {
			speed.modifier = 1.0f;
		}
	}
}
