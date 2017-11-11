package com.vast.collision;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Inventory;
import com.vast.component.Pickup;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerWithPickupCollisionHandler extends AbstractCollisionHandler {
	private static final Logger logger = LoggerFactory.getLogger(PlayerWithPickupCollisionHandler.class);

	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Pickup> pickupMapper;

	public PlayerWithPickupCollisionHandler() {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Pickup.class));
	}

	@Override
	public void handleCollision(int playerEntity, int pickupEntity) {
		inventoryMapper.get(playerEntity).add(pickupMapper.get(pickupEntity).type, 1);
		world.delete(pickupEntity);
	}
}
