package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Known;
import com.vast.component.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNearbyEntityIteratingSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AbstractNearbyEntityIteratingSystem.class);

	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;

	private Set<Integer> reusableNearbyEntities;

	public AbstractNearbyEntityIteratingSystem(Aspect.Builder builder) {
		super(builder);

		reusableNearbyEntities = new HashSet<Integer>();
	}

	@Override
	protected void process(int entity) {
		if (scanMapper.has(entity)) {
			process(entity, scanMapper.get(entity).nearbyEntities);
		} else {
			reusableNearbyEntities.clear();
			IntBag scanEntities = world.getAspectSubscriptionManager().get(Aspect.all(Scan.class)).getEntities();
			for (int i = 0; i < scanEntities.size(); i++) {
				int scanEntity = scanEntities.get(i);
				if (scanMapper.has(scanEntity)) {
					if (scanMapper.get(scanEntity).nearbyEntities.contains(entity)) {
						reusableNearbyEntities.add(scanEntity);
					}
				}
			}
			process(entity, reusableNearbyEntities);
		}
	}

	protected abstract void process(int entity, Set<Integer> nearbyEntities);
}
