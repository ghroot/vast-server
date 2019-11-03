package com.vast.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.vast.component.Time;

public class TimeManager extends BaseSystem {
	private ComponentMapper<Time> timeMapper;

	@All(Time.class)
	private EntitySubscription timeSubscription;

	public float getTime() {
		Time time = timeMapper.get(timeSubscription.getEntities().get(0));
		return time.time;
	}

	public float getPreviousTime() {
		Time time = timeMapper.get(timeSubscription.getEntities().get(0));
		return time.previousTime;
	}

	@Override
	protected void processSystem() {
		Time time = timeMapper.get(timeSubscription.getEntities().get(0));
		time.previousTime = time.time;
		time.time += world.getDelta();
	}
}
