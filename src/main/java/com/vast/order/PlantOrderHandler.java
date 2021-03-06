package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.network.Properties;
import com.vast.network.MessageCodes;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class PlantOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	private final float PLANT_DISTANCE = 1.0f;

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
		if (inventory.has(items.getItem("Seed").getId())) {
			inventory.remove(items.getItem("Seed").getId(), 1);
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

			Transform transform = transformMapper.get(orderEntity);
			Point2f plantPosition = new Point2f(transform.position);
			plantPosition.x += Math.cos(Math.toRadians(transform.rotation)) * PLANT_DISTANCE;
			plantPosition.y += Math.sin(Math.toRadians(transform.rotation)) * PLANT_DISTANCE;
			int treeEntity = creationManager.createTree(plantPosition, true);
			createMapper.create(treeEntity).reason = "planted";

			return true;
		} else {
			eventMapper.create(orderEntity).addEntry("message").setData("I need a seed...").setOwnerPropagation();
			return false;
		}
	}
}
