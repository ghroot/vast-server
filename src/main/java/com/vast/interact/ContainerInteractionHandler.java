package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Item;
import com.vast.data.Items;
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

	private Items items;

	public ContainerInteractionHandler(Items items) {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Container.class, Inventory.class));
		this.items = items;
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
						if (transferAllOrUntilFull(playerInventory, containerInventory, "basic")) {
							syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
							syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
							eventMapper.create(playerEntity).name = "pickedUp";
						}
					}
				} else {
					if (transferAllOrUntilFull(containerInventory, playerInventory)) {
						syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
						syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
						eventMapper.create(playerEntity).name = "pickedUp";
					}
				}
			} else {
				if (transferAllOrUntilFull(containerInventory, playerInventory)) {
					syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
					eventMapper.create(playerEntity).name = "pickedUp";
					deleteMapper.create(containerEntity).reason = "collected";
				}
			}
		}

		return true;
	}

	@Override
	public void stop(int playerEntity, int containerEntity) {
	}

	// TODO: This can end up in an infinite loop
	private boolean transferAllOrUntilFull(Inventory from, Inventory to, String onlyItemType) {
		boolean atLeastOneItemWasTransferred = false;
		while (!from.isEmpty() && !to.isFull()) {
			for (int itemId = 0; itemId < from.items.length; itemId++) {
				Item item = items.getItem(itemId);
				if ((onlyItemType == null || item.getType().equals(onlyItemType)) && from.items[itemId] > 0) {
					from.remove(itemId, 1);
					to.add(itemId, 1);
					atLeastOneItemWasTransferred = true;
					break;
				}
			}
		}
		return atLeastOneItemWasTransferred;
	}

	private boolean transferAllOrUntilFull(Inventory from, Inventory to) {
		return transferAllOrUntilFull(from, to, null);
	}
}
