package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.SpatialHash;
import com.vast.WorldConfiguration;
import com.vast.component.Spatial;
import com.vast.component.Static;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpatialUpdateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SpatialUpdateSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, Set<Integer>> spatialHashes;

	public SpatialUpdateSystem(WorldConfiguration worldConfiguration, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(Transform.class, Spatial.class).exclude(Static.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
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
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.lastUsedPosition == null || !spatial.lastUsedPosition.equals(transform.position)) {
			updateSpatialHash(entity);

			if (spatial.lastUsedPosition == null) {
				spatial.lastUsedPosition = new Point2f(transform.position);
			} else {
				spatial.lastUsedPosition.set(transform.position);
			}
		}
	}

	private void updateSpatialHash(int entity) {
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
}
