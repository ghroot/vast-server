package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.vast.data.Cost;

import java.util.Set;

public class Fueled extends PooledComponent {
	public float timeLeft = 0.0f;
	public Set<Cost> costs = null;
	public String fueledAuraEffectName = null;
	@EntityId public int effectEntity = -1;

	public boolean isFueled() {
		return timeLeft > 0.0f;
	}

	@Override
	protected void reset() {
		timeLeft = 0.0f;
		costs = null;
		fueledAuraEffectName = null;
		effectEntity = -1;
	}
}
