package test.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.CollisionComponent;
import test.component.TransformComponent;

import javax.vecmath.Vector2f;
import java.util.Map;
import java.util.Set;

public class CollisionSystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<CollisionComponent> collisionComponentMapper;

	private Map<Integer, Set<Integer>> nearbyEntitiesByEntity;
	private Vector2f reusableVector;

	public CollisionSystem(Map<Integer, Set<Integer>> nearbyEntitiesByEntity) {
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;
		reusableVector = new Vector2f();
	}

	@Override
	protected void processSystem() {
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(TransformComponent.class, CollisionComponent.class)).getEntities();
		for (int i = 0; i < entities.size(); i++) {
			int entity = entities.get(i);
			TransformComponent transformComponent = transformComponentMapper.get(entity);
			CollisionComponent collisionComponent = collisionComponentMapper.get(entity);
			for (int j = i + 1; j < entities.size(); j++) {
				int otherEntity = entities.get(j);
				if (nearbyEntitiesByEntity.get(entity).contains(otherEntity)) {
					TransformComponent otherTransformComponent = transformComponentMapper.get(otherEntity);
					CollisionComponent otherCollisionComponent = collisionComponentMapper.get(otherEntity);
					reusableVector.set(otherTransformComponent.position.x - transformComponent.position.x, otherTransformComponent.position.y - transformComponent.position.y);
					float overlap = (collisionComponent.radius + otherCollisionComponent.radius) - reusableVector.length();
					if (overlap > 0.0f) {
						if (reusableVector.lengthSquared() == 0.0f) {
							reusableVector.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
						}

						if (collisionComponent.isStatic && !otherCollisionComponent.isStatic) {
							reusableVector.normalize();
							reusableVector.scale(overlap);
							otherTransformComponent.position.add(reusableVector);
						} else if (!collisionComponent.isStatic && otherCollisionComponent.isStatic) {
							reusableVector.normalize();
							reusableVector.scale(-overlap);
							transformComponent.position.add(reusableVector);
						} else {
							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							transformComponent.position.add(reusableVector);

							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							otherTransformComponent.position.add(reusableVector);
						}
					}
				}
			}
		}
	}
}
