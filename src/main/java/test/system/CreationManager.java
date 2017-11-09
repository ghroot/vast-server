package test.system;

import com.artemis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.WorldDimensions;
import test.component.*;

public class CreationManager extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreationManager.class);

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<CollisionComponent> collisionComponentMapper;

	private WorldDimensions worldDimensions;

	private Archetype aiArchetype;
	private Archetype treeArchetype;

	public CreationManager(WorldDimensions worldDimensions) {
		this.worldDimensions = worldDimensions;
	}

	@Override
	protected void initialize() {
		aiArchetype = new ArchetypeBuilder()
				.add(AIComponent.class)
				.add(TypeComponent.class)
				.add(TransformComponent.class)
				.add(SpatialComponent.class)
				.add(CollisionComponent.class)
				.add(SyncTransformComponent.class)
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(TypeComponent.class)
				.add(TransformComponent.class)
				.add(SpatialComponent.class)
				.add(CollisionComponent.class)
				.build(world);
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		for (int i = 0; i < 10000; i++) {
			int aiEntity = world.create(aiArchetype);
			typeComponentMapper.get(aiEntity).type = "ai";
			transformComponentMapper.get(aiEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
			logger.info("Creating AI entity: {}", aiEntity);
		}

		for (int i = 0; i < 500; i++) {
			int treeEntity = world.create(treeArchetype);
			typeComponentMapper.get(treeEntity).type = "tree";
			transformComponentMapper.get(treeEntity).position.set(-worldDimensions.width / 2 + (float) Math.random() * worldDimensions.width, -worldDimensions.height / 2 + (float) Math.random() * worldDimensions.height);
			collisionComponentMapper.get(treeEntity).isStatic = true;
			collisionComponentMapper.get(treeEntity).radius = 0.1f;
			logger.info("Creating tree entity: {}", treeEntity);
		}
	}
}
