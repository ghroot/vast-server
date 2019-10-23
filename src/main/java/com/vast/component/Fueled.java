package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.data.Cost;

public class Fueled extends PooledComponent {
	public float timeLeft = 0f;
	public Cost cost;

	public boolean isFueled() {
		return timeLeft > 0f;
	}

	@Override
	protected void reset() {
		timeLeft = 0.0f;
		cost = null;
	}
}
