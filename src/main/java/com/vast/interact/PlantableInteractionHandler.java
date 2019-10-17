package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.data.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlantableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(PlantableInteractionHandler.class);

	private ComponentMapper<Plantable> plantableMapper;
	private ComponentMapper<Growing> growingMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;
	private ComponentMapper<Event> eventMapper;

	private Items items;

	public PlantableInteractionHandler(Items items) {
		super(Aspect.all(Inventory.class), Aspect.all(Plantable.class));
		this.items = items;
	}

	@Override
	public boolean canInteract(int playerEntity, int plantableEntity) {
		return !growingMapper.has(plantableEntity);
	}

	@Override
	public boolean attemptStart(int playerEntity, int plantableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Plantable plantable = plantableMapper.get(plantableEntity);

		if (!plantable.planted && !inventory.has(items.getItem("seed"))) {
			messageMapper.create(playerEntity).text = "I need a seed...";
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean process(int playerEntity, int plantableEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Plantable plantable = plantableMapper.get(plantableEntity);

		if (plantable.planted) {
			inventory.add(items.getItem("food"), 3);
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
			eventMapper.create(playerEntity).name = "pickedUp";
			plantable.planted = false;
		} else {
			inventory.remove(items.getItem("seed"));
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
			eventMapper.create(playerEntity).name = "pickedUp";
			plantable.planted = true;
			growingMapper.create(plantableEntity).timeLeft = 5.0f;
			syncMapper.create(plantableEntity).markPropertyAsDirty(Properties.GROWING);
		}

		return true;
	}

	@Override
	public void stop(int playerEntity, int plantableEntity) {
	}
}
