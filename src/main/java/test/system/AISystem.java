package test.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.*;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.List;
import java.util.Map;

public class AISystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private final float FOLLOW_DISTANCE = 2.0f;
	private final float FOLLOW_DISTANCE_SQUARED = FOLLOW_DISTANCE * FOLLOW_DISTANCE;

	private ComponentMapper<AIComponent> aiComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;

	private Map<Integer, List<Integer>> nearbyEntitiesByEntity;
	private Archetype aiEntityArchetype;
	private Vector2f reusableVector;

	public AISystem(Map<Integer, List<Integer>> nearbyEntitiesByEntity) {
		super(Aspect.all(AIComponent.class));
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;
		reusableVector = new Vector2f();
	}

	@Override
	protected void initialize() {
		aiEntityArchetype = new ArchetypeBuilder()
				.add(TransformComponent.class)
				.add(SyncTransformComponent.class)
				.add(AIComponent.class)
				.build(world);

		for (int i = 0; i < 10; i++) {
			int entity = world.create(aiEntityArchetype);
			logger.info("Creating AI entity: {}", entity);
		}
	}

	@Override
	protected void process(int entity) {
		AIComponent aiComponent = aiComponentMapper.get(entity);
		if (aiComponent.countdown == 0) {
			if (aiComponent.followingEntity >= 0) {
				aiComponent.countdown = 0.5f;
			} else {
				aiComponent.countdown = (float) (1.0f + Math.random() * 4.0f);
			}
		} else {
			aiComponent.countdown = Math.max(aiComponent.countdown - world.delta, 0);
			if (aiComponent.countdown == 0) {
				if (aiComponent.followingEntity < 0) {
					TransformComponent transformComponent = transformComponentMapper.get(entity);
					List<Integer> nearbyEntities = nearbyEntitiesByEntity.get(entity);
					for (int nearbyEntity : nearbyEntities) {
						if (peerComponentMapper.has(nearbyEntity) && activeComponentMapper.has(nearbyEntity)) {
							TransformComponent nearbyTransformComponent = transformComponentMapper.get(nearbyEntity);
							reusableVector.set(nearbyTransformComponent.position.x - transformComponent.position.x, nearbyTransformComponent.position.y - transformComponent.position.y);
							if (reusableVector.lengthSquared() <= FOLLOW_DISTANCE_SQUARED) {
								logger.info("AI entity {} started following {}", entity, nearbyEntity);
								aiComponent.followingEntity = nearbyEntity;
								break;
							}
						}
					}
				}

				if (aiComponent.followingEntity >= 0) {
					if (!activeComponentMapper.has(aiComponent.followingEntity)) {
						logger.info("AI entity {} stopped following {}", entity, aiComponent.followingEntity);
						aiComponent.followingEntity = -1;
					} else {
						TransformComponent transformComponent = transformComponentMapper.get(entity);
						TransformComponent followTransformComponent = transformComponentMapper.get(aiComponent.followingEntity);
						reusableVector.set(followTransformComponent.position.x - transformComponent.position.x, followTransformComponent.position.y - transformComponent.position.y);
						if (reusableVector.lengthSquared() > FOLLOW_DISTANCE_SQUARED) {
							float distance = reusableVector.length();
							reusableVector.normalize();
							reusableVector.scale(distance - 1.5f);
							Point2f movePosition = new Point2f(transformComponent.position);
							movePosition.add(reusableVector);
							pathComponentMapper.create(entity).targetPosition = new Point2f(movePosition.x - 0.5f + (float) Math.random() * 1.0f, movePosition.y - 0.5f + (float) Math.random() * 1.0f);
						}
					}
				} else {
					pathComponentMapper.create(entity).targetPosition = new Point2f((float) (-2.5f + Math.random() * 5.0f), (float) (-2.5f + Math.random() * 5.0f));
				}
			}
		}
	}
}
