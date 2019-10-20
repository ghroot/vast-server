package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.Metrics;
import com.vast.network.Properties;
import com.vast.data.WorldConfiguration;
import net.mostlyoriginal.api.utils.QuadTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class CollisionSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Static> staticMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Scan> scanMapper;

	private WorldConfiguration worldConfiguration;
	private QuadTree quadTree;
	private Metrics metrics;

	private IntBag reusableNearbyEntities;
	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(WorldConfiguration worldConfiguration, QuadTree quadTree, Metrics metrics) {
		super(Aspect.all(Transform.class, Collision.class).exclude(Static.class));
		this.worldConfiguration = worldConfiguration;
		this.quadTree = quadTree;
		this.metrics = metrics;

		reusableNearbyEntities = new IntBag();
		reusableVector = new Vector2f();
	}

	@Override
	protected void begin() {
		numberOfCollisionChecks = 0;
	}

	@Override
	protected void end() {
		if (metrics != null) {
			metrics.setNumberOfCollisionChecks(numberOfCollisionChecks);
		}
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		Collision collision = collisionMapper.get(entity);

		if (collision.lastCheckedPosition == null || !collision.lastCheckedPosition.equals(transform.position)) {
			if (collision.lastCheckedPosition == null) {
				collision.lastCheckedPosition = new Point2f(transform.position);
			} else {
				collision.lastCheckedPosition.set(transform.position);
			}
			IntBag nearbyEntitiesBag = getNearbyEntities(entity);
			int[] nearbyEntities = nearbyEntitiesBag.getData();
			for (int i = 0, size = nearbyEntitiesBag.size(); i < size; ++i) {
				int nearbyEntity = nearbyEntities[i];
				if (nearbyEntity != entity) {
					if (!collisionMapper.has(nearbyEntity)) {
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

	private IntBag getNearbyEntities(int entity) {
		reusableNearbyEntities.clear();
		if (scanMapper.has(entity)) {
			return scanMapper.get(entity).nearbyEntities;
		} else {
			Transform transform = transformMapper.get(entity);

			float distance = 2f;

			reusableNearbyEntities.clear();
			quadTree.get(reusableNearbyEntities, transform.position.x + worldConfiguration.width / 2f - distance,
				transform.position.y + worldConfiguration.height / 2f - distance, 2f * distance, 2f * distance);
		}
		return reusableNearbyEntities;
	}
}
