package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Active;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.component.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNearbyEntityIteratingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AbstractNearbyEntityIteratingSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Set<Integer> reusableNearbyEntities;

	public AbstractNearbyEntityIteratingSystem(Aspect.Builder builder) {
		super(builder);

		reusableNearbyEntities = new HashSet<Integer>();
	}

	@Override
	protected void process(int entity) {
§		if (scanMapper.has(entity)) {
			process(entity, scanMapper.get(entity).nearbyEntities);
		} else {
			reusableNearbyEntities.clear();
			IntBag nearbyEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class, Scan.class)).getEntities();
			for (int i = 0; i < nearbyEntities.size(); i++) {
				int nearbyEntity = nearbyEntities.get(i);
				if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity) && scanMapper.has(nearbyEntity)) {
					Scan scan = scanMapper.get(nearbyEntity);
					if (scan.nearbyEntities.contains(entity)) {
						reusableNearbyEntities.add(nearbyEntity);
					}
				}
			}
			process(entity, reusableNearbyEntities);
		}
	}

	protected abstract void process(int entity, Set<Integer> nearbyEntities);
}
