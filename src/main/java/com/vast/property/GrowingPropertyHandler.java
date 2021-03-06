package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Growing;
import com.vast.network.Properties;

public class GrowingPropertyHandler extends AbstractPropertyHandler<Boolean, Boolean> {
	private ComponentMapper<Growing> growingMapper;

	public GrowingPropertyHandler() {
		super(Properties.GROWING);
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return growingMapper.has(entity);
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		if (growingMapper.has(entity)) {
			Growing growing = growingMapper.get(entity);
			return !growing.finished && growing.timeLeft > 0f;
		} else {
			return false;
		}
	}
}
