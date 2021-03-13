package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.network.Properties;

public class ValidPropertyHandler extends AbstractPropertyHandler<Boolean, Boolean> {
	private ComponentMapper<Placeholder> placeholderMapper;

	public ValidPropertyHandler() {
		super(Properties.VALID);
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return placeholderMapper.has(entity);
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		return placeholderMapper.get(entity).valid;
	}
}
