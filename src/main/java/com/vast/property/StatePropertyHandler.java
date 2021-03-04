package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.State;
import com.vast.network.Properties;

public class StatePropertyHandler extends AbstractPropertyHandler<String, String> {
	private ComponentMapper<State> stateMapper;

	public StatePropertyHandler() {
		super(Properties.STATE);
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return stateMapper.has(entity);
	}

	@Override
	protected String getPropertyData(int entity) {
		return stateMapper.get(entity).name;
	}
}
