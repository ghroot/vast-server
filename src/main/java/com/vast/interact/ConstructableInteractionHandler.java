package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.Constructable;
import com.vast.component.Event;
import com.vast.component.Player;
import com.vast.component.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConstructableInteractionHandler.class);

	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	public ConstructableInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Constructable.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int constructableEntity) {
		Constructable constructable = constructableMapper.get(constructableEntity);
		return !constructable.isComplete();
	}

	@Override
	public void start(int playerEntity, int constructableEntity) {
		eventMapper.create(playerEntity).name = "startedBuilding";
	}

	@Override
	public boolean process(int playerEntity, int constructableEntity) {
		Constructable constructable = constructableMapper.get(constructableEntity);
		constructable.buildTime += world.getDelta();
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.PROGRESS);
		if (constructable.isComplete()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int buildingEntity) {
		eventMapper.create(playerEntity).name = "stoppedBuilding";
	}
}
