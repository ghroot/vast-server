package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private final float HARVEST_SPEED = 50.0f;

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Message> messageMapper;

	public HarvestableInteractionHandler() {
		super(Aspect.all(Inventory.class), Aspect.all(Harvestable.class, Inventory.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		return harvestable.durability > 0.0f;
	}

	@Override
	public void start(int playerEntity, int harvestableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		if (harvestable.requiredItemId == -1 || inventory.has(harvestable.requiredItemId)) {
			eventMapper.create(playerEntity).name = "startedHarvesting";
			eventMapper.create(harvestableEntity).name = "startedHarvesting";
		}
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		if (harvestable.requiredItemId != -1 && !inventory.has(harvestable.requiredItemId)) {
			messageMapper.create(playerEntity).text = "I don't have the required tool...";
			return true;
		} else if (inventory.isFull()) {
			messageMapper.create(playerEntity).text = "My backpack is full...";
			return true;
		} else {
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
	}

	@Override
	public void stop(int playerEntity, int harvestableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Harvestable harvestable = null;
		if (harvestableEntity != -1) {
			harvestable = harvestableMapper.get(harvestableEntity);
		}

		if (harvestable == null || harvestable.requiredItemId == -1 || inventory.has(harvestable.requiredItemId)) {
			eventMapper.create(playerEntity).name = "stoppedHarvesting";
			if (harvestableEntity != -1) {
				eventMapper.create(harvestableEntity).name = "stoppedHarvesting";
			}
		}
	}
}
