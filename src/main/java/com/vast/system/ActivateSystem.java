package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Active;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.component.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Profile(enabled = true, using = Profiler.class)
public class ActivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ActivateSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;

	private Map<String, VastPeer> peers;

	private EventMessage reusableEventMessage;

	public ActivateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Player.class, Scan.class, Known.class).exclude(Active.class));
		this.peers = peers;

		reusableEventMessage = new EventMessage(MessageCodes.PEER_ENTITY_ACTIVATED);
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
			player.id = peers.get(player.name).getId();
			logger.info("Activating peer entity: {} for {} ({})", inactivePlayerEntity, player.name, player.id);
			activeMapper.create(inactivePlayerEntity);
			for (int nearbyEntity : scanMapper.get(inactivePlayerEntity).nearbyEntities) {
				if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity) && knownMapper.get(nearbyEntity).knownEntities.contains(inactivePlayerEntity)) {
					VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
					reusableEventMessage.getDataObject().set(MessageCodes.PEER_ENTITY_ACTIVATED_ENTITY_ID, inactivePlayerEntity);
					nearbyPeer.send(reusableEventMessage, SendOptions.ReliableSend);
				}
			}
		}
	}
}
