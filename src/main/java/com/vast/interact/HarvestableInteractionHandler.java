package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.system.CreationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private final float HARVEST_SPEED = 50.0f;

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Message> messageMapper;

	private CreationManager creationManager;

	public HarvestableInteractionHandler() {
		super(Aspect.all(Inventory.class), Aspect.all(Harvestable.class, Inventory.class));
	}

	@Override
	public void initialize() {
		super.initialize();

		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public boolean canInteract(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		return harvestable.durability > 0.0f;
	}

	@Override
	public boolean attemptStart(int playerEntity, int harvestableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		if (inventory.isFull()) {
			messageMapper.create(playerEntity).text = "My backpack is full...";
			return false;
		} else if (harvestable.requiredItemId != -1 && !inventory.has(harvestable.requiredItemId)) {
			messageMapper.create(playerEntity).text = "I don't have the required tool...";
			return false;
		} else {
			String capitalizedEventName = "started" + harvestable.harvestEventName.substring(0, 1).toUpperCase() + harvestable.harvestEventName.substring(1);
			eventMapper.create(playerEntity).name = capitalizedEventName;
			eventMapper.create(harvestableEntity).name = capitalizedEventName;
			return true;
		}
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		harvestable.durability -= world.getDelta() * HARVEST_SPEED;
		if (harvestable.durability <= 0.0f) {
			creationManager.createPickup(transformMapper.get(harvestableEntity).position, 0, inventoryMapper.get(harvestableEntity));
			deleteMapper.create(harvestableEntity).reason = "harvested";
			return true;
		} else {
			syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.DURABILITY);
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int harvestableEntity) {
		eventMapper.create(playerEntity).name = "stoppedHarvesting";
		if (harvestableEntity != -1) {
			eventMapper.create(harvestableEntity).name = "stoppedHarvesting";
		}
	}
}
