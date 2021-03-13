package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Fueled;
import com.vast.network.Properties;

public class FueledPropertyHandler extends AbstractPropertyHandler<Boolean, Boolean> {
	private ComponentMapper<Fueled> fueledMapper;

	public FueledPropertyHandler() {
		super(Properties.FUELED);
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return fueledMapper.has(entity);
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		return fueledMapper.get(entity).timeLeft > 0f;
	}
}
