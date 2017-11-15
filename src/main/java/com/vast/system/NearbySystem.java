package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.SpatialHash;
import com.vast.WorldDimensions;
import com.vast.component.Spatial;
import com.vast.component.SyncTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class NearbySystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(NearbySystem.class);

	private ComponentMapper<Spatial> spatialMapper;

	private final int NEARBY_THRESHOLD = 8;

	private Map<Integer, Set<Integer>> nearbyEntitiesByEntity;
	private WorldDimensions worldDimensions;
	private Map<Integer, Set<Integer>> spatialHashes;

	private int sectionsInEachDirection;
	private SpatialHash reusableHash;

	public NearbySystem(Map<Integer, Set<Integer>> nearbyEntitiesByEntity, WorldDimensions worldDimensions, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(SyncTransform.class, Spatial.class));
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;

		sectionsInEachDirection = (int) Math.ceil((float) NEARBY_THRESHOLD / worldDimensions.sectionSize);
		reusableHash = new SpatialHash();
	}

	@Override
	protected void inserted(int spatialEntity) {
		nearbyEntitiesByEntity.put(spatialEntity, new HashSet<Integer>());
	}

	@Override
	protected void removed(int spatialEntity) {
		nearbyEntitiesByEntity.remove(spatialEntity);
	}

	@Override
	protected void process(int spatialEntity) {
		Set<Integer> nearbyEntities = nearbyEntitiesByEntity.get(spatialEntity);
		nearbyEntities.clear();

		Spatial spatial = spatialMapper.get(spatialEntity);
		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.x - sectionsInEachDirection * worldDimensions.sectionSize; x <= spatial.memberOfSpatialHash.x + sectionsInEachDirection * worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - sectionsInEachDirection * worldDimensions.sectionSize; y <= spatial.memberOfSpatialHash.y + sectionsInEachDirection * worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash.uniqueKey())) {
						nearbyEntities.addAll(spatialHashes.get(reusableHash.uniqueKey()));
					}
				}
			}
		}
	}
}
