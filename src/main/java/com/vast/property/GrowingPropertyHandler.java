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
		return true;
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		if (growingMapper.has(entity)) {
			return growingMapper.get(entity).timeLeft > 0f;
		} else {
			return false;
		}
	}
}
