package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Scan;
import com.vast.component.Spatial;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
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

		Set<Integer> nearbyEntities = scan.nearbyEntities;
		nearbyEntities.clear();

		if (spatial.memberOfSpatialHash != null) {
			int sectionSize = worldConfiguration.sectionSize;
			int sectionsInEachDirection = (int) Math.ceil(scan.distance / sectionSize);
			int distanceInEachDirection = sectionsInEachDirection * sectionSize;
			int spatialX = spatial.memberOfSpatialHash.getX();
			int spatialY = spatial.memberOfSpatialHash.getY();
			int startX = spatialX - distanceInEachDirection;
			int endX = spatialX + distanceInEachDirection;
			int startY = spatialY - distanceInEachDirection;
			int endY = spatialY + distanceInEachDirection;
			for (int x = startX; x <= endX; x += sectionSize) {
				for (int y = startY; y <= endY; y += sectionSize) {
					reusableHash.setXY(x, y);
					Set<Integer> entitiesInHash = spatialHashes.get(reusableHash.getUniqueKey());
					if (entitiesInHash != null) {
						nearbyEntities.addAll(entitiesInHash);
					}
				}
			}
		}
	}
}
