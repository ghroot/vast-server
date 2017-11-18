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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActiveSyncHandler extends AbstractSyncHandler {
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<Integer, Boolean> lastSyncedActives;
	private EventMessage reusableEventMessage;

	public ActiveSyncHandler() {
		super(Aspect.all(Player.class));

		lastSyncedActives = new HashMap<Integer, Boolean>();
		reusableEventMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	public void inserted(int entity) {
		lastSyncedActives.put(entity, activeMapper.has(entity));
	}

	@Override
	public void removed(int entity) {
		lastSyncedActives.remove(entity);
	}

	@Override
	public boolean needsSync(int entity) {
		if (playerMapper.has(entity)) {
			boolean active = activeMapper.has(entity);
			boolean lastSyncedActive = lastSyncedActives.get(entity);
			return active != lastSyncedActive;
		} else {
			return false;
		}
	}

	@Override
	public void sync(int entity, Set<VastPeer> nearbyPeers) {
		boolean active = activeMapper.has(entity);
		reusableEventMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, entity);
		reusableEventMessage.getDataObject().set(MessageCodes.PROPERTY_ACTIVE, active);
		for (VastPeer nearbyPeer : nearbyPeers) {
			nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
		}
		lastSyncedActives.put(entity, active);
	}
}
