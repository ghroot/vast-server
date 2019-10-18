package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FueledInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(FueledInteractionHandler.class);

	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

	public FueledInteractionHandler() {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Fueled.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int fueledEntity) {
		return !fueledMapper.get(fueledEntity).isFueled();
	}

	@Override
	public boolean attemptStart(int playerEntity, int fueledEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Fueled fueled = fueledMapper.get(fueledEntity);

		if (!inventory.has(fueled.cost)) {
			messageMapper.create(playerEntity).text = "I don't have the required materials...";
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean process(int playerEntity, int fueledEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Fueled fueled = fueledMapper.get(fueledEntity);

		inventory.remove(fueled.cost);
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);

		fueled.timeLeft = 60.0f;
		syncMapper.create(fueledEntity).markPropertyAsDirty(Properties.FUELED);

		return true;
	}

	@Override
	public void stop(int playerEntity, int fueledEntity) {
	}
}
