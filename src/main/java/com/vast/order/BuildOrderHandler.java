package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.data.Building;
import com.vast.data.Buildings;
import com.vast.data.Properties;
import com.vast.network.MessageCodes;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class BuildOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

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
		int buildingId = (byte) dataObject.get(MessageCodes.BUILD_TYPE).value;
		float[] position = (float[]) dataObject.get(MessageCodes.BUILD_POSITION).value;
		float rotation = (float) dataObject.get(MessageCodes.BUILD_ROTATION).value;
		Building building = buildings.getBuilding(buildingId);
		if (inventory.has(building.getCosts())) {
			inventory.remove(building.getCosts());
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

			Point2f buildPosition = new Point2f(position[0], position[1]);
			int buildingEntity = creationManager.createBuilding(buildPosition, building.getId());
			transformMapper.get(buildingEntity).rotation = rotation;
			ownerMapper.get(buildingEntity).name = playerMapper.get(orderEntity).name;
			createMapper.create(buildingEntity).reason = "built";

			interactMapper.create(orderEntity).entity = buildingEntity;

			return true;
		} else {
			messageMapper.create(orderEntity).text = "I don't have the required materials...";
			return false;
		}
	}
}
