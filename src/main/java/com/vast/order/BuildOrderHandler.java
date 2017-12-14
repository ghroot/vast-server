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
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

	private final float BUILD_DISTANCE = 1.5f;

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
		Building building = buildings.getBuilding(buildingId);
		if (inventory.has(building.getCosts())) {
			inventory.remove(building.getCosts());
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

			Transform transform = transformMapper.get(orderEntity);
			Point2f buildPosition = new Point2f(transform.position);
			buildPosition.x += Math.cos(Math.toRadians(transform.rotation)) * BUILD_DISTANCE;
			buildPosition.y += Math.sin(Math.toRadians(transform.rotation)) * BUILD_DISTANCE;
			int buildingEntity = creationManager.createBuilding(buildPosition, building.getId());
			transformMapper.get(buildingEntity).rotation = (transform.rotation + 180.0f) % 360.0f;
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
