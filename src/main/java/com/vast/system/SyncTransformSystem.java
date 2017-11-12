package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalIteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.SyncTransform;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncTransformSystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncTransformSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncTransform> syncTransformMapper;

	private final float SYNC_THRESHOLD = 0.05f;
	private final float SYNC_THRESHOLD_SQUARED = SYNC_THRESHOLD * SYNC_THRESHOLD;

	private Map<String, VastPeer> peers;
	private Map<String, Set<Integer>> knownEntitiesByPeer;

	private float[] reusablePosition;

	public SyncTransformSystem(Map<String, VastPeer> peers, Map<String, Set<Integer>> knownEntitiesByPeer) {
		super(Aspect.all(Transform.class, SyncTransform.class), 0.2f);
		this.peers = peers;
		this.knownEntitiesByPeer = knownEntitiesByPeer;

		reusablePosition = new float[2];
	}

	@Override
	protected void inserted(int entity) {
		syncTransformMapper.get(entity).lastSyncedPosition.set(transformMapper.get(entity).position);
	}

	@Override
	public void removed(IntBag entities) {
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
			for (VastPeer peer : peers.values()) {
				if (knownEntitiesByPeer.containsKey(peer.getName()) && knownEntitiesByPeer.get(peer.getName()).contains(entity)) {
					peer.send(positionMessage, SendOptions.ReliableSend);
				}
			}
			syncTransform.lastSyncedPosition.set(transform.position);
		}
	}
}
