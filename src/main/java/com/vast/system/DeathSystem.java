package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Profile(enabled = true, using = Profiler.class)
public class DeathSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Create> createMapper;

	private Map<String, Integer> entitiesByPeer;
	private CreationManager creationManager;

	public DeathSystem(Map<String, Integer> entitiesByPeer) {
		super(Aspect.all(Death.class));
		this.entitiesByPeer = entitiesByPeer;
	}

	@Override
	protected void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	protected void process(int deathEntity) {
		logger.debug("Entity {} died", deathEntity);
		if (playerMapper.has(deathEntity)) {
			creationManager.createCrate(transformMapper.get(deathEntity).position);

			String name = playerMapper.get(deathEntity).name;
			int playerEntity = creationManager.createPlayer(name, aiMapper.has(deathEntity));
			activeMapper.create(playerEntity);
			entitiesByPeer.put(name, playerEntity);
			createMapper.create(playerEntity).reason = "resurrected";

			activeMapper.remove(deathEntity);
		}
		deleteMapper.create(deathEntity).reason = "killed";
	}
}
