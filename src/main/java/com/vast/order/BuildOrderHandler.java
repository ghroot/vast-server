package com.vast.order;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.*;

import javax.vecmath.Point2f;

public class BuildOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Building> buildingMapper;
	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;

	private Archetype buildingArcheType;

	@Override
	public void initialize() {
		buildingArcheType = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Building.class)
				.add(Interactable.class)
				.add(Scan.class)
				.build(world);
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
		int buildingEntity = world.create(buildingArcheType);
		typeMapper.get(buildingEntity).type = "building";
		transformMapper.get(buildingEntity).position.set(buildPosition);
		collisionMapper.get(buildingEntity).isStatic = true;
		collisionMapper.get(buildingEntity).radius = 0.5f;
		buildingMapper.get(buildingEntity).type = type;
		createMapper.create(buildingEntity).reason = "built";

		interactMapper.create(orderEntity).entity = buildingEntity;

		return true;
	}
}
