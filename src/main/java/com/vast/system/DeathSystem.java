package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
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
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Disabled> disabledMapper;

	private CreationManager creationManager;

	public DeathSystem() {
		super(Aspect.all(Death.class));
	}

	@Override
	protected void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	protected void process(int deathEntity) {
		Death death = deathMapper.get(deathEntity);

		if (death.countdown > 0.0f) {
			death.countdown -= world.getDelta();
			if (death.countdown <= 0.0f) {
				int playerEntity = creationManager.createPlayer(playerMapper.get(deathEntity).name,
						subTypeMapper.get(deathEntity).subType,
						aiMapper.has(deathEntity));
				knownMapper.get(playerEntity).knownEntities.addAll(knownMapper.get(deathEntity).knownEntities);
				world.delete(deathEntity);
			}
		} else {
			logger.debug("Entity {} died", deathEntity);
			if (inventoryMapper.has(deathEntity)) {
				Inventory inventory = inventoryMapper.get(deathEntity);
				if (!inventory.isEmpty()) {
					creationManager.createCrate(transformMapper.get(deathEntity).position, inventory);
					inventory.clear();
				}
			}
			if (playerMapper.has(deathEntity)) {
				disabledMapper.create(deathEntity);
				eventMapper.create(deathEntity).name = "died";
				deathMapper.get(deathEntity).countdown = 5.0f;
			} else {
				deleteMapper.create(deathEntity).reason = "died";
				deathMapper.remove(deathEntity);
			}
		}
	}
}
