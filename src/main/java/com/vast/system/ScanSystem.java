package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Profiler;
import com.vast.SpatialHash;
import com.vast.WorldDimensions;
import com.vast.component.Scan;
import com.vast.component.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class ScanSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ScanSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldDimensions worldDimensions;
	private Map<Integer, Set<Integer>> spatialHashes;

	private SpatialHash reusableHash;

	public ScanSystem(WorldDimensions worldDimensions, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(Scan.class, Spatial.class));
		this.worldDimensions = worldDimensions;
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

		int sectionsInEachDirection = (int) Math.ceil((float) scan.distance / worldDimensions.sectionSize);

		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.x - sectionsInEachDirection * worldDimensions.sectionSize; x <= spatial.memberOfSpatialHash.x + sectionsInEachDirection * worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - sectionsInEachDirection * worldDimensions.sectionSize; y <= spatial.memberOfSpatialHash.y + sectionsInEachDirection * worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash.uniqueKey())) {
						scan.nearbyEntities.addAll(spatialHashes.get(reusableHash.uniqueKey()));
					}
				}
			}
		}
	}
}
