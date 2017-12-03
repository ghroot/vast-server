package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ContainerInteractionHandler.class);

	private ComponentMapper<Container> containerMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Message> messageMapper;

	public ContainerInteractionHandler() {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Container.class, Inventory.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int containerEntity) {
		if (ownerMapper.has(containerEntity) &&
				!playerMapper.get(playerEntity).name.equals(ownerMapper.get(containerEntity).name)) {
			return false;
		}
		return true;
	}

	@Override
	public void start(int playerEntity, int containerEntity) {
	}

	@Override
	public boolean process(int playerEntity, int containerEntity) {
		Inventory playerInventory = inventoryMapper.get(playerEntity);
		Inventory containerInventory = inventoryMapper.get(containerEntity);

		if (playerInventory.isFull()) {
			messageMapper.create(playerEntity).text = "My backpack is full...";
		} else {
			if (containerMapper.get(containerEntity).persistent) {
				if (containerInventory.isEmpty()) {
					if (!playerInventory.isEmpty()) {
						transferAllOrUntilFull(playerInventory, containerInventory);
						syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
						syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
					}
				} else {
					transferAllOrUntilFull(containerInventory, playerInventory);
					syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
					syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
				}
			} else {
				transferAllOrUntilFull(containerInventory, playerInventory);
				syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
				deleteMapper.create(containerEntity).reason = "collected";
			}

			eventMapper.create(playerEntity).name = "pickedUp";
		}

		return true;
	}

	@Override
	public void stop(int playerEntity, int containerEntity) {
	}

	private void transferAllOrUntilFull(Inventory from, Inventory to) {
		while (!from.isEmpty() && !to.isFull()) {
			for (int itemType = 0; itemType < from.items.length; itemType++) {
				if (from.items[itemType] > 0) {
					from.remove(itemType, 1);
					to.add(itemType, 1);
					break;
				}
			}
		}
	}
}
