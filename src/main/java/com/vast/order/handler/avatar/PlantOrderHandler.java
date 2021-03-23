package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.network.Properties;
import com.vast.network.MessageCodes;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.OrderRequest;
import com.vast.order.request.avatar.EmoteOrderRequest;
import com.vast.order.request.avatar.PlantOrderRequest;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class PlantOrderHandler extends AbstractOrderHandler<PlantOrderRequest> {
	private World world;

	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	private final float PLANT_DISTANCE = 1f;

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
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof PlantOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return true;
	}

	@Override
	public void cancelOrder(int avatarEntity) {
	}

	@Override
	public boolean startOrder(int avatarEntity, PlantOrderRequest plantOrderRequest) {
		Inventory inventory = inventoryMapper.get(avatarEntity);
		if (inventory.has(items.getItem("Seed").getId())) {
			inventory.remove(items.getItem("Seed").getId(), 1);
			syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);

			Transform transform = transformMapper.get(avatarEntity);
			Point2f plantPosition = new Point2f(transform.position);
			plantPosition.x += Math.cos(Math.toRadians(transform.rotation)) * PLANT_DISTANCE;
			plantPosition.y += Math.sin(Math.toRadians(transform.rotation)) * PLANT_DISTANCE;
			int treeEntity = creationManager.createTree(plantPosition, true);
			createMapper.create(treeEntity).reason = "planted";

			return true;
		} else {
			eventMapper.create(avatarEntity).addEntry("message").setData("I need a seed...").setOwnerPropagation();
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int avatarEntity, PlantOrderRequest plantOrderRequest) {
		return false;
	}
}
