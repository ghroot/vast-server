package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.component.Spatial;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SpatialSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SpatialSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldDimensions worldDimensions;
	private Map<Point2i, Set<Integer>> spatialHashes;

	public SpatialSystem(WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(Transform.class, Spatial.class));
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
	}

	@Override
	protected void inserted(int entity) {
		Spatial spatial = spatialMapper.get(entity);

		spatial.memberOfSpatialHash = null;
		spatial.lastUsedPosition = null;
	}

	@Override
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.lastUsedPosition == null || !spatial.lastUsedPosition.equals(transform.position)) {
			if (spatial.memberOfSpatialHash != null) {
				spatialHashes.get(spatial.memberOfSpatialHash).remove(entity);
				spatial.memberOfSpatialHash = null;
			}

			Point2i hash = new Point2i(
					Math.round(transform.position.x / worldDimensions.sectionSize) * worldDimensions.sectionSize,
					Math.round(transform.position.y / worldDimensions.sectionSize) * worldDimensions.sectionSize
			);

			spatial.memberOfSpatialHash = hash;
			spatial.lastUsedPosition = new Point2f(transform.position);

			Set<Integer> entitiesInHash;
			if (spatialHashes.containsKey(hash)) {
				entitiesInHash = spatialHashes.get(hash);
			} else {
				entitiesInHash = new HashSet<Integer>();
				spatialHashes.put(hash, entitiesInHash);
			}
			entitiesInHash.add(entity);
		}
	}
}
