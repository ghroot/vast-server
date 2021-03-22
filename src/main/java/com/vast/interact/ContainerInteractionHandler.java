package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Item;
import com.vast.data.Items;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ContainerInteractionHandler.class);

	private ComponentMapper<Container> containerMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	private Items items;

	public ContainerInteractionHandler(Items items) {
		super(Aspect.all(Avatar.class, Inventory.class), Aspect.all(Container.class, Inventory.class));
		this.items = items;
	}

	@Override
	public boolean canInteract(int avatarEntity, int containerEntity) {
		return true;
	}

	@Override
	public boolean attemptStart(int avatarEntity, int containerEntity) {
		Inventory avatarInventory = inventoryMapper.get(avatarEntity);

		if (ownerMapper.has(containerEntity) && !avatarMapper.get(avatarEntity).name.equals(ownerMapper.get(containerEntity).name)) {
			eventMapper.create(avatarEntity).addEntry("message").setData("This is not mine...").setOwnerPropagation();
			return false;
		} else if (!containerMapper.get(containerEntity).persistent && avatarInventory.isFull()) {
			eventMapper.create(avatarEntity).addEntry("message").setData("My backpack is full...").setOwnerPropagation();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean process(int avatarEntity, int containerEntity) {
		Inventory playerInventory = inventoryMapper.get(avatarEntity);
		Inventory containerInventory = inventoryMapper.get(containerEntity);

		if (containerMapper.get(containerEntity).persistent) {
			if (containerInventory.isEmpty() || playerInventory.isFull()) {
				if (transferAllOrUntilFull(playerInventory, containerInventory, "basic")) {
					syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
					syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);
					eventMapper.create(avatarEntity).addEntry("action").setData("pickedUp");
				}
			} else {
				if (transferAllOrUntilFull(containerInventory, playerInventory)) {
					syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);
					syncMapper.create(containerEntity).markPropertyAsDirty(Properties.INVENTORY);
					eventMapper.create(avatarEntity).addEntry("action").setData("pickedUp");
				}
			}
		} else {
			if (transferAllOrUntilFull(containerInventory, playerInventory)) {
				syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);
				eventMapper.create(avatarEntity).addEntry("action").setData("pickedUp");
				deleteMapper.create(containerEntity).reason = "collected";
			}
		}

		return true;
	}

	@Override
	public void stop(int avatarEntity, int containerEntity) {
	}

	private boolean transferAllOrUntilFull(Inventory from, Inventory to, String onlyItemTag) {
		boolean atLeastOneItemWasTransferred = false;
		while (!from.isEmpty() && !to.isFull() && (onlyItemTag == null || hasItemWithTag(from, onlyItemTag))) {
			for (int itemId = 0; itemId < from.items.length; itemId++) {
				Item item = items.getItem(itemId);
				if (item != null) {
					if ((onlyItemTag == null || item.hasTag(onlyItemTag)) && from.items[itemId] > 0) {
						from.remove(itemId);
						to.add(itemId);
						atLeastOneItemWasTransferred = true;
						break;
					}
				}
			}
		}
		return atLeastOneItemWasTransferred;
	}

	private boolean transferAllOrUntilFull(Inventory from, Inventory to) {
		return transferAllOrUntilFull(from, to, null);
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
}
