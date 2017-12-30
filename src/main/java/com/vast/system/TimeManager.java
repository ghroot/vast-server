package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.vast.component.Time;

public class TimeManager extends BaseSystem {
	private ComponentMapper<Time> timeMapper;

	private EntitySubscription timeSubscription;

	@Override
	protected void initialize() {
		timeSubscription = world.getAspectSubscriptionManager().get(Aspect.all(Time.class));
	}

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
