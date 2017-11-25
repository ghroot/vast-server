package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.SpatialHash;
import com.vast.WorldConfiguration;
import com.vast.component.Scan;
import com.vast.component.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class ScanSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ScanSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, Set<Integer>> spatialHashes;

	private SpatialHash reusableHash;

	public ScanSystem(WorldConfiguration worldConfiguration, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(Scan.class, Spatial.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;

		reusableHash = new SpatialHash();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int scanEntity) {
		Scan scan = scanMapper.get(scanEntity);
		Spatial spatial = spatialMapper.get(scanEntity);

		scan.nearbyEntities.clear();
		int sectionsInEachDirection = (int) Math.ceil(scan.distance / worldConfiguration.sectionSize);
		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.x - sectionsInEachDirection * worldConfiguration.sectionSize; x <= spatial.memberOfSpatialHash.x + sectionsInEachDirection * worldConfiguration.sectionSize; x += worldConfiguration.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - sectionsInEachDirection * worldConfiguration.sectionSize; y <= spatial.memberOfSpatialHash.y + sectionsInEachDirection * worldConfiguration.sectionSize; y += worldConfiguration.sectionSize) {
					reusableHash.set(x, y);
					Set<Integer> entitiesInHash = spatialHashes.get(reusableHash.uniqueKey());
					if (entitiesInHash != null) {
						scan.nearbyEntities.addAll(entitiesInHash);
					}
				}
			}
		}
	}
}
