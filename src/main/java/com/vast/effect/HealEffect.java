package com.vast.effect;

import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.Health;
import com.vast.component.Sync;

import java.util.Set;

public class HealEffect implements Effect {
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<Sync> syncMapper;

	@Override
	public void process(int effectEntity, Set<Integer> nearbyEntities) {
		for (int nearbyEntity : nearbyEntities) {
			if (nearbyEntity != effectEntity && healthMapper.has(nearbyEntity)) {
				Health health = healthMapper.get(nearbyEntity);
				if (!health.isFull()) {
					health.heal(1);
					syncMapper.create(nearbyEntity).markPropertyAsDirty(Properties.HEALTH);
				}
			}
		}
	}
}
