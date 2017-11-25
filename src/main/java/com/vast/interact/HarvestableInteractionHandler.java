package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private final float HARVEST_SPEED = 80.0f;

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	public HarvestableInteractionHandler() {
		super(Aspect.all(Inventory.class), Aspect.all(Harvestable.class, Inventory.class));
	}

	@Override
	public void start(int playerEntity, int harvestableEntity) {
		eventMapper.create(playerEntity).name = "startedHarvesting";
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);
		harvestable.durability -= world.getDelta() * HARVEST_SPEED;
		syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.DURABILITY);
		if (harvestable.durability <= 0.0f) {
			inventoryMapper.get(playerEntity).add(inventoryMapper.get(harvestableEntity));
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
			deleteMapper.create(harvestableEntity).reason = "harvested";
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int harvestableEntity) {
		eventMapper.create(playerEntity).name = "stoppedHarvesting";
	}
}
