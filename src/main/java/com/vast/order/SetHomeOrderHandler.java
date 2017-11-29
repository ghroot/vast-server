package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Home;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.component.Transform;
import com.vast.system.CreationManager;

public class SetHomeOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;

	private CreationManager creationManager;

	public SetHomeOrderHandler() {
	}

	@Override
	public void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.SET_HOME;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.SET_HOME;
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
		homeMapper.create(orderEntity).position.set(transformMapper.get(orderEntity).position);
		return true;
	}
}
