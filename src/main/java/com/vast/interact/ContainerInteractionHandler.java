package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ContainerInteractionHandler.class);

	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;

	public ContainerInteractionHandler() {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Container.class, Inventory.class));
	}

	@Override
	public void start(int playerEntity, int containerEntity) {
	}

	@Override
	public boolean process(int playerEntity, int containerEntity) {
		inventoryMapper.get(playerEntity).add(inventoryMapper.get(containerEntity));
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
		deleteMapper.create(containerEntity).reason = "collected";
		return true;
	}

	@Override
	public void stop(int playerEntity, int containerEntity) {
	}
}
