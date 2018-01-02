package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.component.Sync;
import com.vast.data.Properties;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DeactivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeactivateSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Sync> syncMapper;

	private Map<String, VastPeer> peers;

	public DeactivateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Player.class, Active.class));
		this.peers = peers;
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
		if (!peers.containsKey(player.name) || peers.get(player.name).getId() != player.id) {
			logger.info("Deactivating peer entity: {} for {} ({})", activePlayerEntity, player.name, player.id);
			player.id = 0;
			activeMapper.remove(activePlayerEntity);
			if (knownMapper.has(activePlayerEntity)) {
				knownMapper.get(activePlayerEntity).knownEntities.clear();
			}
			syncMapper.create(activePlayerEntity).markPropertyAsDirty(Properties.ACTIVE);
		}
	}
}
