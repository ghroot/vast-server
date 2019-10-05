package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.data.Time;

public class TimeSystem extends BaseSystem {
	private Time time;

	public TimeSystem(Time time) {
		this.time = time;
	}

	@Override
	protected void processSystem() {
		time.previousTime = time.currentTime;
		time.currentTime += world.getDelta();
	}
}
