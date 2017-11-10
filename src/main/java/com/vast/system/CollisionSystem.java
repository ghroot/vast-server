package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.component.Collision;
import com.vast.component.Spatial;
import com.vast.component.Transform;
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

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Collision> collisionMapper;

	private Map<Point2i, Set<Integer>> spatialHashes;
	private Set<Integer> handledEntities;
	private Vector2f reusableVector;

	public CollisionSystem(Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(Transform.class, Spatial.class, Collision.class));
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

		Spatial spatial = spatialMapper.get(entity);

		if (spatial.memberOfSpatialHash != null) {
			Transform transform = transformMapper.get(entity);
			Collision collision = collisionMapper.get(entity);
			Set<Integer> entitiesInSameSpatialHash = spatialHashes.get(spatial.memberOfSpatialHash);
			for (int entityInSameSpatialHash : entitiesInSameSpatialHash) {
				if (entityInSameSpatialHash != entity) {
					Transform otherTransform = transformMapper.get(entityInSameSpatialHash);
					Collision otherCollision = collisionMapper.get(entityInSameSpatialHash);
					reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
					float overlap = (collision.radius + otherCollision.radius) - reusableVector.length();
					if (overlap > 0.0f) {
						if (reusableVector.lengthSquared() == 0.0f) {
							reusableVector.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
						}

						if (collision.isStatic && !otherCollision.isStatic) {
							reusableVector.normalize();
							reusableVector.scale(overlap);
							otherTransform.position.add(reusableVector);
						} else if (!collision.isStatic && otherCollision.isStatic) {
							reusableVector.normalize();
							reusableVector.scale(-overlap);
							transform.position.add(reusableVector);
						} else if (!collision.isStatic && !otherCollision.isStatic) {
							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							transform.position.add(reusableVector);

							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							otherTransform.position.add(reusableVector);
						}
					}
				}
			}
			handledEntities.addAll(entitiesInSameSpatialHash);
		}
	}
}
