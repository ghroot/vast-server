package test.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.*;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.List;
import java.util.Map;

public class AISystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private final float START_FOLLOWING_DISTANCE = 2.0f;

	private ComponentMapper<AIComponent> aiComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<FollowComponent> followComponentMapper;

	private Map<Integer, List<Integer>> nearbyEntitiesByEntity;
	private Archetype aiEntityArchetype;
	private Vector2f reusableVector;

	public AISystem(Map<Integer, List<Integer>> nearbyEntitiesByEntity) {
		super(Aspect.one(AIComponent.class).exclude(PathComponent.class), 1.0f);
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;
		reusableVector = new Vector2f();
	}

	@Override
	protected void initialize() {
		aiEntityArchetype = new ArchetypeBuilder()
				.add(AIComponent.class)
				.add(TransformComponent.class)
				.add(CollisionComponent.class)
				.add(SyncTransformComponent.class)
				.build(world);

		for (int i = 0; i < 10; i++) {
			int entity = world.create(aiEntityArchetype);
			logger.info("Creating AI entity: {}", entity);
		}
	}

	@Override
	protected void process(int entity) {
		if (followComponentMapper.has(entity)) {
			FollowComponent followComponent = followComponentMapper.get(entity);
			if (followComponent.followingEntity >= 0 &&
					peerComponentMapper.has(followComponent.followingEntity) &&
					!activeComponentMapper.has(followComponent.followingEntity)) {
				logger.info("AI entity {} stopped following {}", entity, followComponent.followingEntity);
				followComponentMapper.remove(entity);
			}
		} else {
			int nearbyActivePeerEntity = findNearbyActivePeerEntity(entity, START_FOLLOWING_DISTANCE);
			if (nearbyActivePeerEntity >= 0) {
				logger.info("AI entity {} started following {}", entity, nearbyActivePeerEntity);
				followComponentMapper.create(entity).followingEntity = nearbyActivePeerEntity;
			} else {
				pathComponentMapper.create(entity).targetPosition = new Point2f((float) (-2.5f + Math.random() * 5.0f), (float) (-2.5f + Math.random() * 5.0f));
			}
		}
	}

	private int findNearbyActivePeerEntity(int entity, float distance) {
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		List<Integer> nearbyEntities = nearbyEntitiesByEntity.get(entity);
		for (int nearbyEntity : nearbyEntities) {
			if (peerComponentMapper.has(nearbyEntity) && activeComponentMapper.has(nearbyEntity)) {
				TransformComponent nearbyTransformComponent = transformComponentMapper.get(nearbyEntity);
				reusableVector.set(nearbyTransformComponent.position.x - transformComponent.position.x, nearbyTransformComponent.position.y - transformComponent.position.y);
				if (reusableVector.length() <= distance) {
					return nearbyEntity;
				}
			}
		}
		return -1;
	}
}
