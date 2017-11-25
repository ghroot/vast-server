package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Death> deathMapper;

	private CreationManager creationManager;
	private WorldConfiguration worldConfiguration;

	public DeathSystem(WorldConfiguration worldConfiguration) {
		super(Aspect.all(Death.class));
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	protected void process(int deathEntity) {
		logger.debug("Entity {} died", deathEntity);
		if (inventoryMapper.has(deathEntity)) {
			Inventory inventory = inventoryMapper.get(deathEntity);
			if (!inventory.isEmpty()) {
				creationManager.createCrate(transformMapper.get(deathEntity).position, inventory);
				inventory.clear();
			}
		}
		if (playerMapper.has(deathEntity)) {
			int playerEntity = creationManager.createPlayer(playerMapper.get(deathEntity).name,
					subTypeMapper.get(deathEntity).subType,
					aiMapper.has(deathEntity));
			knownMapper.get(playerEntity).knownEntities.addAll(knownMapper.get(deathEntity).knownEntities);

			playerMapper.remove(deathEntity);
		}
		deleteMapper.create(deathEntity).reason = "killed";
		deathMapper.remove(deathEntity);
	}
}
