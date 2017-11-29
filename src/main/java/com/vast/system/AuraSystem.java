package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.component.Aura;
import com.vast.effect.Effect;

import java.util.Map;
import java.util.Set;

public class AuraSystem extends AbstractNearbyEntityIteratingSystem {
	private ComponentMapper<Aura> auraMapper;

	private Map<String, Effect> effects;

	public AuraSystem(Map<String, Effect> effects) {
		super(Aspect.all(Aura.class));
		this.effects = effects;
	}

	@Override
	protected void initialize() {
		for (Effect effect : effects.values()) {
			world.inject(effect);
		}
	}

	@Override
	protected void inserted(int auraEntity) {
		Aura aura = auraMapper.get(auraEntity);

		aura.effect = effects.get(aura.effectName);
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int auraEntity, Set<Integer> nearbyEntities) {
		Aura aura = auraMapper.get(auraEntity);

		aura.effect.process(auraEntity, nearbyEntities);
	}
}
