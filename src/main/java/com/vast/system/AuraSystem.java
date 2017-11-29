package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.component.Aura;
import com.vast.component.Transform;
import com.vast.effect.Effect;

import javax.vecmath.Vector2f;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AuraSystem extends AbstractNearbyEntityIteratingSystem {
	private ComponentMapper<Aura> auraMapper;
	private ComponentMapper<Transform> transformMapper;

	private Map<String, Effect> effects;

	private Set<Integer> reusableEntitiesInRange;
	private Vector2f reusableVector;

	public AuraSystem(Map<String, Effect> effects) {
		super(Aspect.all(Aura.class));
		this.effects = effects;

		reusableEntitiesInRange = new HashSet<Integer>();
		reusableVector = new Vector2f();
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
		Transform transform = transformMapper.get(auraEntity);

		reusableEntitiesInRange.clear();
		for (int nearbyEntity : nearbyEntities) {
			Transform nearbyTransform = transformMapper.get(nearbyEntity);
			reusableVector.set(transform.position.x - nearbyTransform.position.x, transform.position.y - nearbyTransform.position.y);
			if (reusableVector.length() <= aura.range) {
				reusableEntitiesInRange.add(nearbyEntity);
			}
		}
		aura.effect.process(auraEntity, reusableEntitiesInRange);
	}
}
