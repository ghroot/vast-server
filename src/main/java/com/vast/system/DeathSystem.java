package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Profile(enabled = true, using = Profiler.class)
public class DeathSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeathSystem.class);

	private ComponentMapper<Death> deathMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Pickup> pickupMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Create> createMapper;

	private Map<String, Integer> entitiesByPeer;
	private WorldConfiguration worldConfiguration;
	private Archetype crateArchetype;
	private Archetype playerEntityArchetype;

	public DeathSystem(Map<String, Integer> entitiesByPeer, WorldConfiguration worldConfiguration) {
		super(Aspect.all(Death.class));
		this.entitiesByPeer = entitiesByPeer;
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		crateArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Pickup.class)
				.build(world);

		playerEntityArchetype = new ArchetypeBuilder()
				.add(Player.class)
				.add(Type.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Scan.class)
				.add(Known.class)
				.add(Interactable.class)
				.add(Attack.class)
				.build(world);
	}

	@Override
	protected void process(int deathEntity) {
		logger.debug("Entity {} died", deathEntity);
		if (playerMapper.has(deathEntity)) {
			int crateEntity = world.create(crateArchetype);
			typeMapper.get(crateEntity).type = "pickup";
			transformMapper.get(crateEntity).position.set(transformMapper.get(deathEntity).position);
			collisionMapper.get(crateEntity).isStatic = true;
			collisionMapper.get(crateEntity).radius = 0.1f;
			pickupMapper.create(crateEntity).type = 2;

			int playerEntity = world.create(playerEntityArchetype);
			playerMapper.get(playerEntity).name = playerMapper.get(deathEntity).name;
			activeMapper.create(playerEntity);
			typeMapper.get(playerEntity).type = "player";
			transformMapper.get(playerEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(playerEntity).radius = 0.3f;
			healthMapper.get(playerEntity).maxHealth = 5;
			healthMapper.get(playerEntity).health = 5;
			if (aiMapper.has(deathEntity)) {
				aiMapper.create(playerEntity);
			}
			entitiesByPeer.put(playerMapper.get(playerEntity).name, playerEntity);
			createMapper.create(playerEntity).reason = "resurrected";

			activeMapper.remove(deathEntity);
			deleteMapper.create(deathEntity).reason = "killed";
		} else {
			deleteMapper.create(deathEntity).reason = "killed";
		}
	}
}
