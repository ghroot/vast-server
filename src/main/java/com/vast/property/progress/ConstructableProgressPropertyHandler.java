package com.vast.property.progress;

import com.artemis.ComponentMapper;
import com.vast.component.Constructable;

public class ConstructableProgressPropertyHandler extends AbstractProgressPropertyHandler {
	private ComponentMapper<Constructable> constructableMapper;

	public ConstructableProgressPropertyHandler(int progressThreshold) {
		super(progressThreshold);
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return constructableMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		Constructable constructable = constructableMapper.get(entity);
		return Math.min((int) Math.floor(100f * constructable.buildTime / constructable.buildDuration), 100);
	}
}
