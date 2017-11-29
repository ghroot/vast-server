package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;

public class DeathSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Lifetime> lifetimeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Death> deathMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Disabled> disabledMapper;
	private ComponentMapper<Home> homeMapper;

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
				Player player = playerMapper.get(deathEntity);

				Point2f respawnPosition = null;
				if (homeMapper.has(deathEntity)) {
					respawnPosition = homeMapper.get(deathEntity).position;
				}

				int playerEntity = creationManager.createPlayer(player.name,
					subTypeMapper.get(deathEntity).subType,
					respawnPosition,
					aiMapper.has(deathEntity));
				knownMapper.get(playerEntity).knownEntities.addAll(knownMapper.get(deathEntity).knownEntities);

				world.delete(deathEntity);
			}
		} else {
			logger.debug("Entity {} died", deathEntity);
			if (inventoryMapper.has(deathEntity)) {
				Inventory inventory = inventoryMapper.get(deathEntity);
				if (!inventory.isEmpty()) {
					int crateEntity = creationManager.createCrate(transformMapper.get(deathEntity).position, inventory);
					lifetimeMapper.create(crateEntity).timeLeft = 180.0f;
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
