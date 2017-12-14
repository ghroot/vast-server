package com.vast.interact;

import com.artemis.Aspect;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(PlayerInteractionHandler.class);

	public PlayerInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Player.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int otherPlayerEntity) {
		return true;
	}

	@Override
	public boolean attemptStart(int playerEntity, int otherPlayerEntity) {
		return true;
	}

	@Override
	public boolean process(int playerEntity, int otherPlayerEntity) {
		return false;
	}

	@Override
	public void stop(int playerEntity, int otherPlayerEntity) {
	}
}
