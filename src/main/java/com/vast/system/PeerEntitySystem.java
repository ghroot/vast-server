package com.vast.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;
import com.vast.component.Avatar;
import com.vast.network.FakePeer;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PeerEntitySystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(PeerEntitySystem.class);

	private ComponentMapper<Avatar> avatarMapper;
	private CreationManager creationManager;

	@All(Avatar.class)
	private EntitySubscription avatarSubscription;

	private Map<String, VastPeer> peers;

	private Map<String, Integer> entitiesByPeer;

	public PeerEntitySystem(Map<String, VastPeer> peers, Map<String, Integer> entitiesByPeer) {
		this.peers = peers;
		this.entitiesByPeer = entitiesByPeer;
	}

	@Override
	protected void processSystem() {
		for (VastPeer peer : peers.values()) {
			if (!entitiesByPeer.containsKey(peer.getName())) {
				if (!tryConnectingToExistingAvatarEntity(peer)) {
					createNewPeerEntity(peer);
				}
			}
		}
	}

	private boolean tryConnectingToExistingAvatarEntity(VastPeer peer) {
		IntBag avatarEntities = avatarSubscription.getEntities();
		for (int i = 0; i < avatarEntities.size(); i++) {
			int avatarEntity = avatarEntities.get(i);
			Avatar avatar = avatarMapper.get(avatarEntity);
			if (avatar.name.equals(peer.getName())) {
				entitiesByPeer.put(peer.getName(), avatarEntity);
				logger.info("Connecting existing peer entity: {} for {}", avatarEntity, peer.getName());
				return true;
			}
		}

		return false;
	}

	private void createNewPeerEntity(VastPeer peer) {
		int avatarEntity = creationManager.createAvatar(peer.getName(), Math.abs(peer.getName().hashCode()) % 2);
		entitiesByPeer.put(peer.getName(), avatarEntity);
		logger.info("Creating new peer entity: {} for {}", avatarEntity, peer.getName());
	}
}
