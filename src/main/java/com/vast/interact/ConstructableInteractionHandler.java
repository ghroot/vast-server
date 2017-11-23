package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConstructableInteractionHandler.class);

	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Event> eventMapper;

	public ConstructableInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Constructable.class));
	}

	@Override
	public void start(int playerEntity, int buildingEntity) {
		eventMapper.create(playerEntity).name = "startedBuilding";
	}

	@Override
	public boolean process(int playerEntity, int buildingEntity) {
		Constructable constructable = constructableMapper.get(buildingEntity);
		constructable.buildTime += world.getDelta();
		syncMapper.create(buildingEntity).markPropertyAsDirty(Properties.PROGRESS);
		if (constructable.buildTime >= constructable.buildDuration) {
			interactableMapper.remove(buildingEntity);
			syncMapper.create(buildingEntity).markPropertyAsDirty(Properties.INTERACTABLE);
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
