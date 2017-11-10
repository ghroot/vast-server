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

	private WorldDimensions worldDimensions;

	private Archetype aiArchetype;
	private Archetype treeArchetype;

	public CreationManager(WorldDimensions worldDimensions) {
		this.worldDimensions = worldDimensions;
	}

	@Override
	protected void initialize() {
		aiArchetype = new ArchetypeBuilder()
				.add(AI.class)
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(SyncTransform.class)
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < 10000; i++) {
			int aiEntity = world.create(aiArchetype);
			typeMapper.get(aiEntity).type = "ai";
			transformMapper.get(aiEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
		}

		for (int i = 0; i < 500; i++) {
			int treeEntity = world.create(treeArchetype);
			typeMapper.get(treeEntity).type = "tree";
			transformMapper.get(treeEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
			collisionMapper.get(treeEntity).isStatic = true;
			collisionMapper.get(treeEntity).radius = 0.1f;
		}
	}
}
