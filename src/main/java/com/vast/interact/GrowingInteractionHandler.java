package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Avatar;
import com.vast.component.Event;
import com.vast.component.Growing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrowingInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GrowingInteractionHandler.class);

	private ComponentMapper<Event> eventMapper;

	public GrowingInteractionHandler() {
		super(Aspect.all(Avatar.class), Aspect.all(Growing.class));
	}

	@Override
	public boolean canInteract(int avatarEntity, int growEntity) {
		return true;
	}

	@Override
	public boolean attemptStart(int avatarEntity, int growEntity) {
		eventMapper.create(avatarEntity).addEntry("message").setData("It is still growing...").setOwnerPropagation();
		return false;
	}

	@Override
	public boolean process(int avatarEntity, int growEntity) {
		return true;
	}

	@Override
	public void stop(int avatarEntity, int growEntity) {
	}
}
