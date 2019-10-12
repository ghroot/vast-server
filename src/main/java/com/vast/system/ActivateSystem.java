package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.Properties;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ActivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(ActivateSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Know> knowMapper;

	private Map<String, VastPeer> peers;

	public ActivateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Player.class).exclude(Active.class));
		this.peers = peers;
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
			syncMapper.create(inactivePlayerEntity).markPropertyAsDirty(Properties.ACTIVE);
			scanMapper.create(inactivePlayerEntity);
			knowMapper.create(inactivePlayerEntity);
		}
	}
}
