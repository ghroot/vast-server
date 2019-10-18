package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.Metrics;
import com.vast.network.Properties;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.Map;

public class CollisionSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CollisionSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Static> staticMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Scan> scanMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, IntBag> spatialHashes;
	private Metrics metrics;

	private SpatialHash reusableHash;
	private IntBag reusableNearbyEntities;
	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(WorldConfiguration worldConfiguration, Map<Integer, IntBag> spatialHashes, Metrics metrics) {
		super(Aspect.all(Transform.class, Spatial.class, Collision.class).exclude(Static.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
		this.metrics = metrics;

		reusableHash = new SpatialHash();
		reusableNearbyEntities = new IntBag();
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
			IntBag nearbyEntitiesBag = getNearbyEntities(entity);
			int[] nearbyEntities = nearbyEntitiesBag.getData();
			for (int i = 0, size = nearbyEntitiesBag.size(); i < size; ++i) {
				int nearbyEntity = nearbyEntities[i];
				if (nearbyEntity != entity) {
					if (!collisionMapper.has(nearbyEntity) || deleteMapper.has(nearbyEntity)) {
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
			Spatial spatial = spatialMapper.get(entity);
			if (spatial.memberOfSpatialHash != null) {
				int sectionSize = worldConfiguration.sectionSize;
				int spatialX = spatial.memberOfSpatialHash.getX();
				int spatialY = spatial.memberOfSpatialHash.getY();
				int startX = spatialX - sectionSize;
				int endX = spatialX + sectionSize;
				int startY = spatialY - sectionSize;
				int endY = spatialY + sectionSize;
				for (int x = startX; x <= endX; x += sectionSize) {
					for (int y = startY; y <= endY; y += sectionSize) {
						reusableHash.setXY(x, y);
						IntBag entitiesInHash = spatialHashes.get(reusableHash.getUniqueKey());
						if (entitiesInHash != null) {
							reusableNearbyEntities.addAll(entitiesInHash);
						}
					}
				}
			}
		}
		return reusableNearbyEntities;
	}
}
