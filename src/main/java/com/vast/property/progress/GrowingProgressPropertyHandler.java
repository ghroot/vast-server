package com.vast.property.progress;

import com.artemis.ComponentMapper;
import com.vast.component.Growing;

public class GrowingProgressPropertyHandler extends AbstractProgressPropertyHandler {
	private ComponentMapper<Growing> growingMapper;

	public GrowingProgressPropertyHandler(int progressThreshold) {
		super(progressThreshold);
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return growingMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		Growing growing = growingMapper.get(entity);
		float timePassed = 10f - growing.timeLeft;
		return Math.min((int) Math.floor(100f * timePassed / 10f), 100);
	}
}
