package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.component.Player;

public abstract class AbstractOrderSystem extends BaseSystem {
	private ComponentMapper<Player> playerMapper;

	@Override
	protected abstract void processSystem();

	protected int getEntityWithPeerName(String name) {
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
		for (int i = 0; i < entities.size(); i++) {
			int entity = entities.get(i);
			Player peer = playerMapper.get(entity);
			if (peer.name.equals(name)) {
				return entity;
			}
		}
		return -1;
	}
}
