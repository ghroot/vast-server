package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.SpatialHash;
import com.vast.WorldConfiguration;
import com.vast.component.Spatial;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpatialShiftSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SpatialShiftSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, Set<Integer>> spatialHashes;

	public SpatialShiftSystem(WorldConfiguration worldConfiguration, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(Transform.class, Spatial.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
	}

	@Override
	public void inserted(int entity) {
		addSpatialHash(entity);
	}

	@Override
	protected void removed(int entity) {
		removeSpatialHash(entity);
	}

	private void addSpatialHash(int entity) {
		Transform transform = transformMapper.get(entity);
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.memberOfSpatialHash != null) {
			spatialHashes.get(spatial.memberOfSpatialHash.uniqueKey()).remove(entity);
		} else {
			spatial.memberOfSpatialHash = new SpatialHash();
		}

		spatial.memberOfSpatialHash.x = Math.round(transform.position.x / worldConfiguration.sectionSize) * worldConfiguration.sectionSize;
		spatial.memberOfSpatialHash.y = Math.round(transform.position.y / worldConfiguration.sectionSize) * worldConfiguration.sectionSize;

		Set<Integer> entitiesInHash = spatialHashes.get(spatial.memberOfSpatialHash.uniqueKey());
		if (entitiesInHash == null) {
			entitiesInHash = new HashSet<Integer>();
			spatialHashes.put(spatial.memberOfSpatialHash.uniqueKey(), entitiesInHash);
		}
		entitiesInHash.add(entity);
	}

	private void removeSpatialHash(int entity) {
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.memberOfSpatialHash != null) {
			spatialHashes.get(spatial.memberOfSpatialHash.uniqueKey()).remove(entity);
			spatial.memberOfSpatialHash = null;
		}
	}

	@Override
	protected void process(int entity) {
	}
}
