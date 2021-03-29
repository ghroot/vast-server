package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Avatar;
import com.vast.component.Constructable;
import com.vast.component.State;
import com.vast.component.Sync;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConstructableInteractionHandler.class);

	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<State> stateMapper;

	public ConstructableInteractionHandler() {
		super(Aspect.all(Avatar.class), Aspect.all(Constructable.class));
	}

	@Override
	public boolean canInteract(int avatarEntity, int constructableEntity) {
		return constructableMapper.has(constructableEntity);
	}

	@Override
	public boolean attemptStart(int avatarEntity, int constructableEntity) {
		stateMapper.get(avatarEntity).name = "building";
		syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.STATE);
		stateMapper.get(constructableEntity).name = "building";
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.STATE);
		return true;
	}

	@Override
	public boolean process(int avatarEntity, int constructableEntity) {
		Constructable constructable = constructableMapper.get(constructableEntity);

		if (constructable.isComplete()) {
			constructableMapper.remove(constructableEntity);
			return true;
		} else {
			constructable.buildTime += world.getDelta();
			syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.PROGRESS);
			return false;
		}
	}

	@Override
	public void stop(int avatarEntity, int constructableEntity) {
		stateMapper.get(avatarEntity).name = "none";
		syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.STATE);
		stateMapper.get(constructableEntity).name = "none";
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.STATE);
	}
}
