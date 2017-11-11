package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Metrics;
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.collision.CollisionHandler;
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

	private Set<CollisionHandler> collisionHandlers;
	private WorldDimensions worldDimensions;
	private Map<Point2i, Set<Integer>> spatialHashes;
	private Metrics metrics;

	private Set<Integer> checkedEntites;
	private Point2i reusableCheckedEntity;
	private Point2i reusableHash;
	private Set<Integer> reusableNearbyEntities;
	private Vector2f reusableVector;
	private int numberOfCollisionChecks;

	public CollisionSystem(Set<CollisionHandler> collisionHandlers, WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes, Metrics metrics) {
		super(Aspect.all(Transform.class, Spatial.class, Collision.class));
		this.collisionHandlers = collisionHandlers;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
		this.metrics = metrics;
		checkedEntites = new HashSet<Integer>();
		reusableCheckedEntity = new Point2i();
		reusableHash = new Point2i();
		reusableNearbyEntities = new HashSet<Integer>();
		reusableVector = new Vector2f();
	}

	@Override
	protected void initialize() {
		for (CollisionHandler collisionHandler : collisionHandlers) {
			collisionHandler.setWorld(world);
			world.inject(collisionHandler);
		}
	}

	@Override
	protected void begin() {
		checkedEntites.clear();
		numberOfCollisionChecks = 0;
	}

	@Override
	protected void end() {
		metrics.setNumberOfCollisionChecks(numberOfCollisionChecks);
	}

	@Override
	protected void process(int entity) {
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.memberOfSpatialHash != null) {
			Transform transform = transformMapper.get(entity);
			Collision collision = collisionMapper.get(entity);
			reusableNearbyEntities.clear();
			for (int x = spatial.memberOfSpatialHash.x - worldDimensions.sectionSize; x <= spatial.memberOfSpatialHash.x + worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - worldDimensions.sectionSize; y <= spatial.memberOfSpatialHash.y + worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash)) {
						reusableNearbyEntities.addAll(spatialHashes.get(reusableHash));
					}
				}
			}
			for (int nearbyEntity : reusableNearbyEntities) {
				if (nearbyEntity != entity) {
					reusableCheckedEntity.set(entity, nearbyEntity);
					int checkedEntity = reusableCheckedEntity.hashCode();
					if (!checkedEntites.contains(checkedEntity)) {
						Transform otherTransform = transformMapper.get(nearbyEntity);
						Collision otherCollision = collisionMapper.get(nearbyEntity);
						reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
						float overlap = (collision.radius + otherCollision.radius) - reusableVector.length();
						if (overlap > 0.0f) {
							boolean handled = false;
							for (CollisionHandler collisionHandler : collisionHandlers) {
								if (collisionHandler.getAspect1().isInterested(world.getEntity(entity)) &&
										collisionHandler.getAspect2().isInterested(world.getEntity(nearbyEntity))) {
									collisionHandler.handleCollision(entity, nearbyEntity);
									handled = true;
								} else if (collisionHandler.getAspect1().isInterested(world.getEntity(nearbyEntity)) &&
										collisionHandler.getAspect2().isInterested(world.getEntity(entity))) {
									collisionHandler.handleCollision(nearbyEntity, entity);
									handled = true;
								}
							}
							if (!handled) {
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
						checkedEntites.add(checkedEntity);
						numberOfCollisionChecks++;
					}
				}
			}
		}
	}
}
