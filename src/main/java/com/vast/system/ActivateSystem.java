package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.network.Properties;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ActivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ActivateSystem.class);

	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Parent> parentMapper;
	private CreationManager creationManager;

	private Map<String, VastPeer> peers;

	public ActivateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Avatar.class).exclude(Observed.class));
		this.peers = peers;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int avatarEntity) {
		Avatar avatar = avatarMapper.get(avatarEntity);
		if (peers.containsKey(avatar.name)) {
			VastPeer peer = peers.get(avatar.name);
			logger.info("Activating peer entity: {} for {} ({})", avatarEntity, avatar.name, peer.getId());

			int observerEntity = creationManager.createObserver(peer, avatarEntity);
			parentMapper.create(observerEntity).parentEntity = avatarEntity;

			observedMapper.create(avatarEntity).observerEntity = observerEntity;
			syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.ACTIVE);
		}
	}
}
