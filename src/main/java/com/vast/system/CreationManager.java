package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.vast.WorldConfiguration;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreationManager extends ProfiledBaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Pickup> pickupMapper;
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Health> healthMapper;

	private WorldConfiguration worldConfiguration;

	private Archetype treeArchetype;
	private Archetype pickupArchetype;
	private Archetype aiArchetype;

	public CreationManager(WorldConfiguration worldConfiguration) {
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		super.initialize();

		treeArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Interactable.class)
				.add(Harvestable.class)
				.build(world);

		pickupArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Pickup.class)
				.build(world);

		aiArchetype = new ArchetypeBuilder()
				.add(AI.class)
				.add(Type.class)
				.add(Inventory.class)
				.add(Health.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Scan.class)
				.add(Interactable.class)
				.add(Attack.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < worldConfiguration.numberOfTrees; i++) {
			int treeEntity = world.create(treeArchetype);
			typeMapper.get(treeEntity).type = "tree";
			transformMapper.get(treeEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(treeEntity).isStatic = true;
			collisionMapper.get(treeEntity).radius = 0.1f;
			harvestableMapper.get(treeEntity).itemType = 2;
			harvestableMapper.get(treeEntity).itemCount = 4;
		}

		for (int i = 0; i < worldConfiguration.numberOfPickups; i++) {
			int pickupEntity = world.create(pickupArchetype);
			typeMapper.get(pickupEntity).type = "pickup";
			transformMapper.get(pickupEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(pickupEntity).isStatic = true;
			collisionMapper.get(pickupEntity).radius = 0.1f;
			pickupMapper.create(pickupEntity).type = 3;
		}

		for (int i = 0; i < worldConfiguration.numberOfAIs; i++) {
			int aiEntity = world.create(aiArchetype);
			typeMapper.get(aiEntity).type = "ai";
			transformMapper.get(aiEntity).position.set(-worldConfiguration.width / 2 + (float) Math.random() * worldConfiguration.width, -worldConfiguration.height / 2 + (float) Math.random() * worldConfiguration.height);
			collisionMapper.get(aiEntity).radius = 0.1f;
			healthMapper.get(aiEntity).maxHealth = 3;
			healthMapper.get(aiEntity).health = 3;
		}
	}
}
