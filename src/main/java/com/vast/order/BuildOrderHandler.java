package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Create;
import com.vast.component.Interact;
import com.vast.component.Order;
import com.vast.component.Path;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class BuildOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;

	private CreationManager creationManager;

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
		String type = (String) dataObject.get((MessageCodes.BUILD_TYPE)).value;
		float[] position = (float[]) dataObject.get(MessageCodes.BUILD_POSITION).value;
		Point2f buildPosition = new Point2f(position[0], position[1]);
		int buildingEntity = creationManager.createBuilding(type, buildPosition);
		createMapper.create(buildingEntity).reason = "built";

		interactMapper.create(orderEntity).entity = buildingEntity;

		return true;
	}
}
