package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.data.Metrics;
import com.vast.component.*;
import com.vast.data.Properties;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollisionSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Static> staticMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, IntBag> spatialHashes;
	private Metrics metrics;

	private SpatialHash reusableHash;
	private IntBag reusableAdjacentEntities;
	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(WorldConfiguration worldConfiguration, Map<Integer, IntBag> spatialHashes, Metrics metrics) {
		super(Aspect.all(Transform.class, Spatial.class, Collision.class).exclude(Static.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
		this.metrics = metrics;

		reusableHash = new SpatialHash();
		reusableAdjacentEntities = new IntBag();
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
			IntBag adjacentEntitiesBag = getAdjacentEntities(entity);
			int[] adjacentEntities = adjacentEntitiesBag.getData();
			for (int i = 0, size = adjacentEntitiesBag.size(); i < size; ++i) {
				int adjacentEntity = adjacentEntities[i];
				if (adjacentEntity != entity) {
					if (!collisionMapper.has(adjacentEntity) || deleteMapper.has(adjacentEntity)) {
						continue;
					}

					Transform nearbyTransform = transformMapper.get(adjacentEntity);
					Collision nearbyCollision = collisionMapper.get(adjacentEntity);
					reusableVector.set(nearbyTransform.position.x - transform.position.x, nearbyTransform.position.y - transform.position.y);
					float overlap = (collision.radius + nearbyCollision.radius) - reusableVector.length();
					if (overlap > 0.0f) {
						if (reusableVector.lengthSquared() == 0.0f) {
							reusableVector.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
						}
						if (staticMapper.has(adjacentEntity)) {
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
							syncMapper.create(adjacentEntity).markPropertyAsDirty(Properties.POSITION);
						}
					}

					numberOfCollisionChecks++;
				}
			}
		}
	}

	private IntBag getAdjacentEntities(int entity) {
		reusableAdjacentEntities.clear();
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.getX() - worldConfiguration.sectionSize; x <= spatial.memberOfSpatialHash.getX() + worldConfiguration.sectionSize; x += worldConfiguration.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.getY() - worldConfiguration.sectionSize; y <= spatial.memberOfSpatialHash.getY() + worldConfiguration.sectionSize; y += worldConfiguration.sectionSize) {
					reusableHash.setXY(x, y);
					IntBag entitiesInHash = spatialHashes.get(reusableHash.getUniqueKey());
					if (entitiesInHash != null) {
						reusableAdjacentEntities.addAll(entitiesInHash);
					}
				}
			}
		}
		return reusableAdjacentEntities;
	}
}
