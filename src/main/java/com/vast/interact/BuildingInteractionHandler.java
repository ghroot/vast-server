package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildingInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(BuildingInteractionHandler.class);

	private ComponentMapper<Building> buildingMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Event> eventMapper;

	public BuildingInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Building.class));
	}

	@Override
	public void start(int playerEntity, int buildingEntity) {
		eventMapper.create(playerEntity).name = "startedBuilding";
	}

	@Override
	public boolean process(int playerEntity, int buildingEntity) {
		Building building = buildingMapper.get(buildingEntity);
		building.progress++;
		if (building.progress % 25 == 0) {
			logger.debug("Player entity {} is building entity {}, progress: {}", playerEntity, buildingEntity, building.progress);
		}
		syncMapper.create(buildingEntity).markPropertyAsDirty(Properties.PROGRESS);
		if (building.progress >= 100) {
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
