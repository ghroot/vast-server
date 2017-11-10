package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.component.CollisionComponent;
import com.vast.component.SpatialComponent;
import com.vast.component.TransformComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2i;
import javax.vecmath.Vector2f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class CollisionSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SpatialComponent> spatialComponentMapper;
	private ComponentMapper<CollisionComponent> collisionComponentMapper;

	private Map<Point2i, Set<Integer>> spatialHashes;
	private Set<Integer> handledEntities;
	private Vector2f reusableVector;

	public CollisionSystem(Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(TransformComponent.class, SpatialComponent.class, CollisionComponent.class));
		this.spatialHashes = spatialHashes;
		handledEntities = new HashSet<Integer>();
		reusableVector = new Vector2f();
	}

	@Override
	protected void end() {
		handledEntities.clear();
	}

	@Override
	protected void process(int entity) {
		if (handledEntities.contains(entity)) {
			return;
		} else {
			handledEntities.add(entity);
		}

		SpatialComponent spatialComponent = spatialComponentMapper.get(entity);

		if (spatialComponent.memberOfSpatialHash != null) {
			TransformComponent transformComponent = transformComponentMapper.get(entity);
			CollisionComponent collisionComponent = collisionComponentMapper.get(entity);
			Set<Integer> entitiesInSameSpatialHash = spatialHashes.get(spatialComponent.memberOfSpatialHash);
			for (int entityInSameSpatialHash : entitiesInSameSpatialHash) {
				if (entityInSameSpatialHash != entity) {
					TransformComponent otherTransformComponent = transformComponentMapper.get(entityInSameSpatialHash);
					CollisionComponent otherCollisionComponent = collisionComponentMapper.get(entityInSameSpatialHash);
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
						} else if (!collisionComponent.isStatic && !otherCollisionComponent.isStatic) {
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
			handledEntities.addAll(entitiesInSameSpatialHash);
		}
	}
}
