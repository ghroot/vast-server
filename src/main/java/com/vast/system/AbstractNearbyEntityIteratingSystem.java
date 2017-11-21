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

	private Aspect.Builder nearbyBuilder;
	private Aspect nearbyAspect;
	private Set<Integer> reusableNearbyEntities;

	public AbstractNearbyEntityIteratingSystem(Aspect.Builder builder, Aspect.Builder nearbyBuilder) {
		super(builder);
		this.nearbyBuilder = nearbyBuilder;

		reusableNearbyEntities = new HashSet<Integer>();
	}

	@Override
	protected void initialize() {
		nearbyAspect = nearbyBuilder.build(world);
	}

	@Override
	protected void process(int entity) {
		reusableNearbyEntities.clear();
		if (scanMapper.has(entity)) {
			for (int nearbyEntity : scanMapper.get(entity).nearbyEntities) {
				if (nearbyAspect.isInterested(world.getEntity(nearbyEntity))) {
					reusableNearbyEntities.add(nearbyEntity);
				}
			}
		} else {
			IntBag nearbyEntities = world.getAspectSubscriptionManager().get(nearbyBuilder).getEntities();
			for (int i = 0; i < nearbyEntities.size(); i++) {
				int nearbyEntity = nearbyEntities.get(i);
				if (scanMapper.has(nearbyEntity)) {
					Scan scan = scanMapper.get(nearbyEntity);
					if (scan.nearbyEntities.contains(entity)) {
						reusableNearbyEntities.add(nearbyEntity);
					}
				}
			}
		}
		process(entity, reusableNearbyEntities);
	}

	protected abstract void process(int entity, Set<Integer> nearbyEntities);
}
