package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Properties;
import com.vast.system.CreationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private final float HARVEST_SPEED = 50.0f;

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;
	private ComponentMapper<State> stateMapper;

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

		if (harvestable.requiredItemId != -1 && !inventory.has(harvestable.requiredItemId)) {
			messageMapper.create(playerEntity).text = "I don't have the required tool...";
			return false;
		} else {
			stateMapper.get(playerEntity).name = harvestable.harvestEventName;
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
			stateMapper.get(harvestableEntity).name = harvestable.harvestEventName;
			syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.STATE);
			return true;
		}
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		harvestable.durability -= world.getDelta() * HARVEST_SPEED;
		if (harvestable.durability <= 0.0f) {
			int pickupEntity = creationManager.createPickup(transformMapper.get(harvestableEntity).position, 0, inventoryMapper.get(harvestableEntity));
			createMapper.create(pickupEntity).reason = "harvested";
			deleteMapper.create(harvestableEntity).reason = "harvested";
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int harvestableEntity) {
		stateMapper.get(playerEntity).name = null;
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
		if (harvestableEntity != -1) {
			stateMapper.get(harvestableEntity).name = null;
			syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.STATE);
		}
	}
}
