package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.vast.WorldDimensions;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreationManager extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Pickup> pickupMapper;
	private ComponentMapper<Harvestable> harvestableMapper;

	private WorldDimensions worldDimensions;

	private Archetype treeArchetype;
	private Archetype pickupArchetype;
	private Archetype aiArchetype;

	public CreationManager(WorldDimensions worldDimensions) {
		this.worldDimensions = worldDimensions;
	}

	@Override
	protected void initialize() {
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
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(SyncTransform.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < 100; i++) {
			int treeEntity = world.create(treeArchetype);
			typeMapper.get(treeEntity).type = "tree";
			transformMapper.get(treeEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
			collisionMapper.get(treeEntity).isStatic = true;
			collisionMapper.get(treeEntity).radius = 0.1f;
			harvestableMapper.get(treeEntity).itemType = 2;
			harvestableMapper.get(treeEntity).itemCount = 4;
		}

		for (int i = 0; i < 10; i++) {
			int pickupEntity = world.create(pickupArchetype);
			typeMapper.get(pickupEntity).type = "pickup";
			transformMapper.get(pickupEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
			collisionMapper.get(pickupEntity).isStatic = true;
			collisionMapper.get(pickupEntity).radius = 0.1f;
			pickupMapper.create(pickupEntity).type = 3;
		}

		for (int i = 0; i < 5; i++) {
			int aiEntity = world.create(aiArchetype);
			typeMapper.get(aiEntity).type = "ai";
			transformMapper.get(aiEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
		}
	}
}
