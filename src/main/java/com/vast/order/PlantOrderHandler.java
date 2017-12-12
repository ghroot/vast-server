package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.system.CreationManager;

public class PlantOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

	private Items items;

	private CreationManager creationManager;

	public PlantOrderHandler(Items items) {
		this.items = items;
	}

	@Override
	public void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.PLANT;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int orderEntity) {
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		Inventory inventory = inventoryMapper.get(orderEntity);
		if (inventory.has(items.getItem("seed").getId())) {
			inventory.remove(items.getItem("seed").getId(), 1);
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

			int treeEntity = creationManager.createTree(transformMapper.get(orderEntity).position, true);
			createMapper.create(treeEntity).reason = "planted";

			return true;
		} else {
			messageMapper.create(orderEntity).text = "I need a seed...";
			return false;
		}
	}
}
