package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.*;
import com.vast.collision.CollisionHandler;
import com.vast.component.*;
import com.vast.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
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
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;

	private Set<CollisionHandler> collisionHandlers;
	private WorldConfiguration worldConfiguration;
	private Map<Integer, Set<Integer>> spatialHashes;
	private Metrics metrics;

	private SpatialHash reusableHash;
	private Set<Integer> reusableAdjacentEntities;
	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(Set<CollisionHandler> collisionHandlers, WorldConfiguration worldConfiguration, Map<Integer, Set<Integer>> spatialHashes, Metrics metrics) {
		super(Aspect.all(Transform.class, Spatial.class, Collision.class));
		this.collisionHandlers = collisionHandlers;
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
		this.metrics = metrics;

		reusableHash = new SpatialHash();
		reusableAdjacentEntities = new HashSet<Integer>();
		reusableVector = new Vector2f();
	}

	@Override
	protected void initialize() {
		for (CollisionHandler collisionHandler : collisionHandlers) {
			world.inject(collisionHandler);
			collisionHandler.initialize();
		}
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
			for (int adjacentEntity : getAdjacentEntities(entity)) {
				if (adjacentEntity != entity) {
					if (deleteMapper.has(entity) || deleteMapper.has(adjacentEntity)) {
						continue;
					}

					Transform adjacentTransform = transformMapper.get(adjacentEntity);
					Collision adjacentCollision = collisionMapper.get(adjacentEntity);
					reusableVector.set(adjacentTransform.position.x - transform.position.x, adjacentTransform.position.y - transform.position.y);
					float overlap = (collision.radius + adjacentCollision.radius) - reusableVector.length();
					if (overlap > 0.0f) {
						boolean handled = false;
						for (CollisionHandler collisionHandler : collisionHandlers) {
							if (collisionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
									collisionHandler.getAspect2().isInterested(world.getEntity(adjacentEntity))) {
								collisionHandler.handleCollision(entity, adjacentEntity);
								handled = true;
							} else if (collisionHandler.getAspect1().isInterested(world.getEntity(adjacentEntity)) &&
									collisionHandler.getAspect2().isInterested(world.getEntity(entity))) {
								collisionHandler.handleCollision(adjacentEntity, entity);
								handled = true;
							}
						}
						if (!handled) {
							if (reusableVector.lengthSquared() == 0.0f) {
								reusableVector.set(-1.0f + (float) Math.random() * 2.0f, -1.0f + (float) Math.random() * 2.0f);
							}
							if (collision.isStatic && !adjacentCollision.isStatic) {
								reusableVector.normalize();
								reusableVector.scale(overlap);
								adjacentTransform.position.add(reusableVector);
								syncMapper.create(adjacentEntity).markPropertyAsDirty(Properties.POSITION);
							} else if (!collision.isStatic && adjacentCollision.isStatic) {
								reusableVector.normalize();
								reusableVector.scale(-overlap);
								transform.position.add(reusableVector);
								syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
							} else if (!collision.isStatic && !adjacentCollision.isStatic) {
								reusableVector.normalize();
								reusableVector.scale(-overlap * 0.5f);
								transform.position.add(reusableVector);
								syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);

								reusableVector.normalize();
								reusableVector.scale(-overlap * 0.5f);
								adjacentTransform.position.add(reusableVector);
								syncMapper.create(adjacentEntity).markPropertyAsDirty(Properties.POSITION);
							}
						}
						numberOfCollisionChecks++;
					}
				}
			}
		}
	}

	private Set<Integer> getAdjacentEntities(int entity) {
		reusableAdjacentEntities.clear();
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.x - worldConfiguration.sectionSize; x <= spatial.memberOfSpatialHash.x + worldConfiguration.sectionSize; x += worldConfiguration.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - worldConfiguration.sectionSize; y <= spatial.memberOfSpatialHash.y + worldConfiguration.sectionSize; y += worldConfiguration.sectionSize) {
					reusableHash.set(x, y);
					Set<Integer> entitiesInHash = spatialHashes.get(reusableHash.uniqueKey());
					if (entitiesInHash != null) {
						reusableAdjacentEntities.addAll(entitiesInHash);
					}
				}
			}
		}
		return reusableAdjacentEntities;
	}
}
