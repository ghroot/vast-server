package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.SyncTransform;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncTransformSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncTransformSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncTransform> syncTransformMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private final float SYNC_THRESHOLD = 0.1f;
	private final float SYNC_THRESHOLD_SQUARED = SYNC_THRESHOLD * SYNC_THRESHOLD;

	private Map<String, VastPeer> peers;
	private Map<Integer, Set<Integer>> nearbyEntitiesByEntity;

	private float[] reusablePosition;
	private EventMessage reusableEventMessage;

	public SyncTransformSystem(Map<String, VastPeer> peers, Map<Integer, Set<Integer>> nearbyEntitiesByEntity) {
		super(Aspect.all(Transform.class, SyncTransform.class));
		this.peers = peers;
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;

		reusablePosition = new float[2];
		reusableEventMessage = new EventMessage(MessageCodes.SET_POSITION, new DataObject());
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
			reusableEventMessage.getDataObject().set(MessageCodes.SET_POSITION_ENTITY_ID, entity);
			reusableEventMessage.getDataObject().set(MessageCodes.SET_POSITION_POSITION, reusablePosition);
			Set<Integer> nearbyEntities = nearbyEntitiesByEntity.get(entity);
			for (int nearbyEntity : nearbyEntities) {
				if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
					VastPeer peer = peers.get(playerMapper.get(nearbyEntity).name);
					if (peer != null) {
						peer.send(reusableEventMessage, SendOptions.ReliableSend);
					}
				}
			}
			syncTransform.lastSyncedPosition.set(transform.position);
		}
	}
}
