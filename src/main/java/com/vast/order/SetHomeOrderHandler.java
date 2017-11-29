package com.vast.order;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.component.Home;
import com.vast.component.Order;
import com.vast.component.Player;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class SetHomeOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Home> homeMapper;
	private ComponentMapper<Player> playerMapper;

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
		Player player = playerMapper.get(orderEntity);

		IntBag homeEntities = world.getAspectSubscriptionManager().get(Aspect.all(Home.class)).getEntities();
		for (int i = 0; i < homeEntities.size(); i++) {
			int homeEntity = homeEntities.get(i);
			if (homeMapper.has(homeEntity)) {
				if (homeMapper.get(homeEntity).name.equals(player.name)) {
					world.delete(homeEntity);
					break;
				}
			}
		}

		float[] position = (float[]) dataObject.get(MessageCodes.SET_HOME_POSITION).value;
		creationManager.createHome(new Point2f(position[0], position[1]), player.name);

		return true;
	}
}
