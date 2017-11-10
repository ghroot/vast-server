package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalIteratingSystem;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.component.Player;
import com.vast.component.Spatial;
import com.vast.component.SyncTransform;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2i;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncTransformSystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncTransformSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncTransform> syncTransformMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private final float SYNC_THRESHOLD = 0.05f;
	private final float SYNC_THRESHOLD_SQUARED = SYNC_THRESHOLD * SYNC_THRESHOLD;

	private Map<String, VastPeer> peers;
	private WorldDimensions worldDimensions;
	private Map<Point2i, Set<Integer>> spatialHashes;

	private float[] reusablePosition;
	private Point2i reusableHash;
	private Set<Integer> reusableNearbyPeerEntities;

	public SyncTransformSystem(Map<String, VastPeer> peers, WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(Transform.class, SyncTransform.class), 0.2f);
		this.peers = peers;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
		reusablePosition = new float[2];
		reusableHash = new Point2i();
		reusableNearbyPeerEntities = new HashSet<Integer>();
	}

	@Override
	protected void inserted(int entity) {
		syncTransformMapper.get(entity).lastSyncedPosition.set(transformMapper.get(entity).position);
	}

	@Override
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		SyncTransform syncTransform = syncTransformMapper.get(entity);

		if (transform.position.distanceSquared(syncTransform.lastSyncedPosition) >= SYNC_THRESHOLD_SQUARED) {
			reusablePosition[0] = transform.position.x;
			reusablePosition[1] = transform.position.y;
			EventMessage positionMessage = new EventMessage(MessageCodes.SET_POSITION, new DataObject()
					.set(MessageCodes.SET_POSITION_ENTITY_ID, entity)
					.set(MessageCodes.SET_POSITION_POSITION, reusablePosition));
			Set<Integer> nearbyPeerEntities = getNearbyPeerEntities(entity);
			for (int nearbyPeerEntity : nearbyPeerEntities) {
				VastPeer peer = peers.get(playerMapper.get(nearbyPeerEntity).name);
				if (peer != null) {
					peer.send(positionMessage, SendOptions.ReliableSend);
				}
			}
			syncTransform.lastSyncedPosition.set(transform.position);
		}
	}

	private Set<Integer> getNearbyPeerEntities(int entity) {
		reusableNearbyPeerEntities.clear();
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.memberOfSpatialHash != null) {
			for (int x = spatial.memberOfSpatialHash.x - worldDimensions.sectionSize; x <= spatial.memberOfSpatialHash.x + worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatial.memberOfSpatialHash.y - worldDimensions.sectionSize; y <= spatial.memberOfSpatialHash.y + worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash)) {
						for (int nearbyEntity : spatialHashes.get(reusableHash)) {
							if (playerMapper.has(nearbyEntity)) {
								reusableNearbyPeerEntities.add(nearbyEntity);
							}
						}
					}
				}
			}
		}
		return reusableNearbyPeerEntities;
	}
}
