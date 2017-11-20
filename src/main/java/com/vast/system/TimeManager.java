package com.vast.system;

import com.artemis.BaseSystem;

public class TimeManager extends BaseSystem {
	private float time;

	@Override
	protected void initialize() {
		time = 0.0f;
	}

	public float getTime() {
		return time;
	}

	@Override
	protected void processSystem() {
		time += world.getDelta();
	}
}
