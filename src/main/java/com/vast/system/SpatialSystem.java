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
	private Map<Integer, Set<Integer>> spatialHashes;

	public SpatialSystem(WorldDimensions worldDimensions, Map<Integer, Set<Integer>> spatialHashes) {
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
	protected void removed(int entity) {
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.memberOfSpatialHash != null) {
			spatialHashes.get(spatial.memberOfSpatialHash.hashCode()).remove(entity);
		}
	}

	@Override
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.lastUsedPosition == null || !spatial.lastUsedPosition.equals(transform.position)) {
			if (spatial.memberOfSpatialHash != null) {
				spatialHashes.get(spatial.memberOfSpatialHash.hashCode()).remove(entity);
			} else {
				spatial.memberOfSpatialHash = new Point2i();
			}

			spatial.memberOfSpatialHash.x = Math.round(transform.position.x / worldDimensions.sectionSize) * worldDimensions.sectionSize;
			spatial.memberOfSpatialHash.y = Math.round(transform.position.y / worldDimensions.sectionSize) * worldDimensions.sectionSize;

			Set<Integer> entitiesInHash;
			if (spatialHashes.containsKey(spatial.memberOfSpatialHash.hashCode())) {
				entitiesInHash = spatialHashes.get(spatial.memberOfSpatialHash.hashCode());
			} else {
				entitiesInHash = new HashSet<Integer>();
				spatialHashes.put(spatial.memberOfSpatialHash.hashCode(), entitiesInHash);
			}
			entitiesInHash.add(entity);

			spatial.lastUsedPosition = new Point2f(transform.position);
		}
	}
}
