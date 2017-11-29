package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.effect.Effect;

public class Aura extends PooledComponent {
	public float range = 0.0f;
	public String effectName = null;
	public transient Effect effect = null;

	@Override
	protected void reset() {
		range = 0.0f;
		effectName = null;
		effect = null;
	}
}
