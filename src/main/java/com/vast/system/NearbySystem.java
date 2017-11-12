package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Spatial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2i;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class NearbySystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(NearbySystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private final int NEARBY_THRESHOLD = 4;

	private Map<String, Set<Integer>> nearbyEntitiesByPeer;
	private WorldDimensions worldDimensions;
	private Map<Integer, Set<Integer>> spatialHashes;

	private Point2i reusableHash;

	public NearbySystem(Map<String, Set<Integer>> nearbyEntitiesByPeer, WorldDimensions worldDimensions, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(Player.class, Active.class));
		this.nearbyEntitiesByPeer = nearbyEntitiesByPeer;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;

		reusableHash = new Point2i();
	}

	@Override
	protected void inserted(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		nearbyEntitiesByPeer.put(player.name, new HashSet<Integer>());
	}

	@Override
	protected void removed(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		nearbyEntitiesByPeer.remove(player.name);
	}

	@Override
	protected void process(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);

		Set<Integer> nearbyEntities = nearbyEntitiesByPeer.get(player.name);
		nearbyEntities.clear();

		Spatial spatial = spatialMapper.get(activePlayerEntity);
		if (spatial.memberOfSpatialHash != null) {
			int n = (int) Math.ceil((float) NEARBY_THRESHOLD / worldDimensions.sectionSize);
			for (int x = spatial.memberOfSpatialHash.x - n * worldDimensions.sectionSize; x <= spatial.memberOfSpatialHash.x + 2 * n * worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - n * worldDimensions.sectionSize; y <= spatial.memberOfSpatialHash.y + 2 * n * worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash.hashCode())) {
						nearbyEntities.addAll(spatialHashes.get(reusableHash.hashCode()));
					}
				}
			}
		}
	}
}
