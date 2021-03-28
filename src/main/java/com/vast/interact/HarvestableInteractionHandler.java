package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Item;
import com.vast.data.Items;
import com.vast.network.Properties;
import com.vast.system.CreationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private final float BASE_HARVEST_SPEED = 30f;

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<State> stateMapper;
	private ComponentMapper<Skill> skillMapper;

	private CreationManager creationManager;

	private Items items;

	public HarvestableInteractionHandler(Items items) {
		super(Aspect.all(Inventory.class), Aspect.all(Harvestable.class, Inventory.class));
		this.items = items;
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

		if (harvestable.requiredItemTag != null && !hasItemWithTag(inventory, harvestable.requiredItemTag)) {
			eventMapper.create(playerEntity).addEntry("message").setData("I don't have the required tool...").setOwnerPropagation();
			return false;
		} else {
			if (harvestable.stateName != null) {
				stateMapper.get(playerEntity).name = harvestable.stateName;
				syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
				stateMapper.get(harvestableEntity).name = harvestable.stateName;
				syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.STATE);
			}

			return true;
		}
	}

	private boolean hasItemWithTag(Inventory inventory, String tag) {
		for (int itemId = 0; itemId < inventory.items.length; itemId++) {
			if (inventory.items[itemId] > 0) {
				Item item = items.getItem(itemId);
				if (item.hasTag(tag)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		if (harvestable.requiredItemTag != null && skillMapper.has(playerEntity)) {
			skillMapper.get(playerEntity).addXp(harvestable.requiredItemTag);
		}

		float harvestSpeed = BASE_HARVEST_SPEED + 5f * skillMapper.get(playerEntity).getLevel(harvestable.requiredItemTag);
		harvestable.durability -= world.getDelta() * harvestSpeed;
		if (harvestable.isComplete()) {
			int pickupEntity = creationManager.createPickup("harvestedResources",
					transformMapper.get(harvestableEntity).position, inventoryMapper.get(harvestableEntity));
			createMapper.create(pickupEntity).reason = "harvested";
			deleteMapper.create(harvestableEntity).reason = "harvested";
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);

		if (harvestable.stateName != null) {
			stateMapper.get(playerEntity).name = "none";
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
			if (harvestableEntity != -1) {
				stateMapper.get(harvestableEntity).name = "none";
				syncMapper.create(harvestableEntity).markPropertyAsDirty(Properties.STATE);
			}
		}
	}
}
