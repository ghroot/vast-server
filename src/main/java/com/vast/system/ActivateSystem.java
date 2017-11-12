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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class ActivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ActivateSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<String, VastPeer> peers;
	private Map<String, Set<Integer>> knownEntitiesByPeer;

	public ActivateSystem(Map<String, VastPeer> peers, Map<String, Set<Integer>> knownEntitiesByPeer) {
		super(Aspect.one(Player.class).exclude(Active.class));
		this.peers = peers;
		this.knownEntitiesByPeer = knownEntitiesByPeer;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int inactivePlayerEntity) {
		Player player = playerMapper.get(inactivePlayerEntity);
		if (peers.containsKey(player.name)) {
			logger.info("Activating peer entity: {} for {}", inactivePlayerEntity, player.name);
			activeMapper.create(inactivePlayerEntity);
			for (VastPeer peerToSendTo : peers.values()) {
				if (knownEntitiesByPeer.containsKey(peerToSendTo.getName()) && knownEntitiesByPeer.get(peerToSendTo.getName()).contains(inactivePlayerEntity)) {
					peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_ACTIVATED, new DataObject()
									.set(MessageCodes.PEER_ENTITY_ACTIVATED_ENTITY_ID, inactivePlayerEntity)),
							SendOptions.ReliableSend);
				}
			}
		}
	}
}
