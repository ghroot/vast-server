package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Metrics;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class CollisionSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Static> staticMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Sync> syncMapper;

	private Metrics metrics;

	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(Metrics metrics) {
		super(Aspect.all(Transform.class, Collision.class, Scan.class).exclude(Static.class));
		this.metrics = metrics;

		reusableVector = new Vector2f();
	}

	@Override
	protected void begin() {
		numberOfCollisionChecks = 0;
	}

	@Override
	protected void end() {
		metrics.setNumberOfCollisionChecks(numberOfCollisionChecks);
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int entity) {
		if (deleteMapper.has(entity)) {
			return;
		}

		Transform transform = transformMapper.get(entity);
		Collision collision = collisionMapper.get(entity);

		if (collision.lastCheckedPosition == null || !collision.lastCheckedPosition.equals(transform.position)) {
			if (collision.lastCheckedPosition == null) {
				collision.lastCheckedPosition = new Point2f(transform.position);
			} else {
				collision.lastCheckedPosition.set(transform.position);
			}
			Scan scan = scanMapper.get(entity);
			for (int nearbyEntity : scan.nearbyEntities) {
				if (nearbyEntity != entity) {
					if (!collisionMapper.has(nearbyEntity) || deleteMapper.has(entity) || deleteMapper.has(nearbyEntity)) {
						continue;
					}

					Transform nearbyTransform = transformMapper.get(nearbyEntity);
					Collision nearbyCollision = collisionMapper.get(nearbyEntity);
					reusableVector.set(nearbyTransform.position.x - transform.position.x, nearbyTransform.position.y - transform.position.y);
					float overlap = (collision.radius + nearbyCollision.radius) - reusableVector.length();
					if (overlap > 0.0f) {
						if (reusableVector.lengthSquared() == 0.0f) {
							reusableVector.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
						}
						if (staticMapper.has(nearbyEntity)) {
							reusableVector.normalize();
							reusableVector.scale(-overlap);
							transform.position.add(reusableVector);
							syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
						} else {
							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							transform.position.add(reusableVector);
							syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);

							reusableVector.normalize();
							reusableVector.scale(-overlap * 0.5f);
							nearbyTransform.position.add(reusableVector);
							syncMapper.create(nearbyEntity).markPropertyAsDirty(Properties.POSITION);
						}
					}

					numberOfCollisionChecks++;
				}
			}
		}
	}
}
