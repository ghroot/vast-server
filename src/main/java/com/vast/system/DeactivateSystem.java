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
public class DeactivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeactivateSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Map<String, VastPeer> peers;
	private Map<String, Set<Integer>> knownEntitiesByPeer;

	public DeactivateSystem(Map<String, VastPeer> peers, Map<String, Set<Integer>> knownEntitiesByPeer) {
		super(Aspect.all(Player.class, Active.class));
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
	protected void process(int activePlayerEntity) {
		Player player = playerMapper.get(activePlayerEntity);
		if (!peers.containsKey(player.name)) {
			logger.info("Deactivating peer entity: {} for {}", activePlayerEntity, player.name);
			activeMapper.remove(activePlayerEntity);
			for (VastPeer peerToSendTo : peers.values()) {
				if (knownEntitiesByPeer.containsKey(peerToSendTo.getName()) && knownEntitiesByPeer.get(peerToSendTo.getName()).contains(activePlayerEntity)) {
					peerToSendTo.send(new EventMessage(MessageCodes.PEER_ENTITY_DEACTIVATED, new DataObject()
									.set(MessageCodes.PEER_ENTITY_DEACTIVATED_ENTITY_ID, activePlayerEntity)),
							SendOptions.ReliableSend);
				}
			}
		}
	}
}
