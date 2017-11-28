package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Interact;
import com.vast.component.Order;
import com.vast.component.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractOrderHandler implements OrderHandler {
	private static final Logger logger = LoggerFactory.getLogger(InteractOrderHandler.class);

	private World world;

	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;

	public InteractOrderHandler() {
	}

	@Override
	public void initialize() {
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.INTERACT;
	}

	@Override
	public Order.Type getOrderType() {
		return Order.Type.INTERACT;
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
		int otherEntity = (int) dataObject.get(MessageCodes.INTERACT_ENTITY_ID).value;
		interactMapper.create(orderEntity).entity = otherEntity;
		return true;
	}
}
