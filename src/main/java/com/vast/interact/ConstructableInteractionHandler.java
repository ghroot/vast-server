package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Constructable;
import com.vast.component.Player;
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
		super(Aspect.all(Player.class), Aspect.all(Constructable.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int constructableEntity) {
		return !constructableMapper.get(constructableEntity).isComplete();
	}

	@Override
	public boolean attemptStart(int playerEntity, int constructableEntity) {
		stateMapper.get(playerEntity).name = "building";
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
		stateMapper.get(constructableEntity).name = "building";
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.STATE);
		return true;
	}

	@Override
	public boolean process(int playerEntity, int constructableEntity) {
		Constructable constructable = constructableMapper.get(constructableEntity);
		constructable.buildTime += world.getDelta();
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.PROGRESS);
		if (constructable.isComplete()) {
			constructableMapper.remove(constructableEntity);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int constructableEntity) {
		stateMapper.get(playerEntity).name = "none";
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
		stateMapper.get(constructableEntity).name = "none";
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.STATE);
	}
}
