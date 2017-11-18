package com.vast.sync;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Harvestable;
import com.vast.component.Scan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DurabilitySyncHandler extends AbstractSyncHandler {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Harvestable> harvestableMapper;

	private Map<Integer, Integer> lastSyncedDurabilities;
	private EventMessage reusableEventMessage;

	public DurabilitySyncHandler() {
		super(Aspect.all(Harvestable.class));

		lastSyncedDurabilities = new HashMap<Integer, Integer>();
		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	public void inserted(int entity) {
		lastSyncedDurabilities.put(entity, harvestableMapper.get(entity).durability);
	}

	@Override
	public void removed(int entity) {
		lastSyncedDurabilities.remove(entity);
	}

	@Override
	public void sync(int entity, Set<VastPeer> nearbyPeers) {
		if (harvestableMapper.has(entity)) {
			int lastSyncedDurability = lastSyncedDurabilities.get(entity);
			int durability = harvestableMapper.get(entity).durability;
			if (durability != lastSyncedDurability) {
				reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, entity);
				reusableEventMessage.getDataObject().set(MessageCodes.PROPERTY_DURABILITY, durability);
				for (VastPeer nearbyPeer : nearbyPeers) {
					nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
				}
				lastSyncedDurabilities.put(entity, durability);
			}
		}
	}
}
