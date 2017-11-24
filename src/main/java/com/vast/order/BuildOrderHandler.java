package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Building;
import com.vast.data.Buildings;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class BuildOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Sync> syncMapper;

	private Buildings buildings;

	private CreationManager creationManager;

	public BuildOrderHandler(Buildings buildings) {
		this.buildings = buildings;
	}

	@Override
	public void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.BUILD;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.BUILD;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !interactMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		interactMapper.remove(orderEntity);
		pathMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		Inventory inventory = inventoryMapper.get(orderEntity);
		int buildingType = (byte) dataObject.get((MessageCodes.BUILD_TYPE)).value;
		Building building = buildings.getBuilding(buildingType);
		if (inventory.has(building.getCosts())) {
			inventory.remove(building.getCosts());
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);
			float[] position = (float[]) dataObject.get(MessageCodes.BUILD_POSITION).value;
			Point2f buildPosition = new Point2f(position[0], position[1]);
			int buildingEntity = creationManager.createBuilding(buildPosition, building.getType());
			interactableMapper.create(buildingEntity);
			syncMapper.create(buildingEntity).markPropertyAsDirty(Properties.INTERACTABLE);
			createMapper.create(buildingEntity).reason = "built";

			interactMapper.create(orderEntity).entity = buildingEntity;

			return true;
		} else {
			return false;
		}
	}
}
