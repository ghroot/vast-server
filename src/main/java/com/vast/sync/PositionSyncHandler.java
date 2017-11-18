package com.vast.sync;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Scan;
import com.vast.component.Transform;

import javax.vecmath.Point2f;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PositionSyncHandler extends AbstractSyncHandler {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;

	private final float SYNC_THRESHOLD = 0.1f;
	private final float SYNC_THRESHOLD_SQUARED = SYNC_THRESHOLD * SYNC_THRESHOLD;

	private Map<Integer, Point2f> lastSyncedPositions;
	private float[] reusablePosition;
	private EventMessage reusableEventMessage;

	public PositionSyncHandler() {
		super(Aspect.all(Transform.class));

		lastSyncedPositions = new HashMap<Integer, Point2f>();
		reusablePosition = new float[2];
		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	public void inserted(int entity) {
		lastSyncedPositions.put(entity, new Point2f(transformMapper.get(entity).position));
	}

	@Override
	public void removed(int entity) {
		lastSyncedPositions.remove(entity);
	}

	@Override
	public boolean needsSync(int entity) {
		if (transformMapper.has(entity)) {
			Transform transform = transformMapper.get(entity);
			Point2f lastSyncedPosition = lastSyncedPositions.get(entity);
			return transform.position.distanceSquared(lastSyncedPosition) >= SYNC_THRESHOLD_SQUARED;
		} else {
			return false;
		}
	}

	@Override
	public void sync(int entity, Set<VastPeer> nearbyPeers) {
		Transform transform = transformMapper.get(entity);
		reusablePosition[0] = transform.position.x;
		reusablePosition[1] = transform.position.y;
		reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, entity);
		reusableEventMessage.getDataObject().set(MessageCodes.PROPERTY_POSITION, reusablePosition);
		for (VastPeer nearbyPeer : nearbyPeers) {
			nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
		}
		lastSyncedPositions.get(entity).set(transform.position);
	}
}
