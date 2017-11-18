package com.vast.sync;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Building;
import com.vast.component.Scan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgressSyncHandler extends AbstractSyncHandler {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Building> buildingMapper;

	private Map<Integer, Integer> lastSyncedProgresses;
	private EventMessage reusableEventMessage;

	public ProgressSyncHandler() {
		super(Aspect.all(Building.class));

		lastSyncedProgresses = new HashMap<Integer, Integer>();
		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	public void inserted(int entity) {
		lastSyncedProgresses.put(entity, buildingMapper.get(entity).progress);
	}

	@Override
	public void removed(int entity) {
		lastSyncedProgresses.remove(entity);
	}

	@Override
	public boolean needsSync(int entity) {
		if (buildingMapper.has(entity)) {
			int progress = buildingMapper.get(entity).progress;
			int lastSyncedProgress = lastSyncedProgresses.get(entity);
			return progress != lastSyncedProgress;
		} else {
			return false;
		}
	}

	@Override
	public void sync(int entity, Set<VastPeer> nearbyPeers) {
		int progress = buildingMapper.get(entity).progress;
		reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, entity);
		reusableEventMessage.getDataObject().set(MessageCodes.PROPERTY_PROGRESS, progress);
		for (VastPeer nearbyPeer : nearbyPeers) {
			nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
		}
		lastSyncedProgresses.put(entity, progress);
	}
}
