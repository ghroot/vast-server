package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Avatar;
import com.vast.component.Observed;
import com.vast.network.Properties;

// TODO: Is this property handler even needed? The client doesn't listen for active property changes!
public class ActivePropertyHandler extends AbstractPropertyHandler<Boolean, Boolean> {
	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Observed> observedMapper;

	public ActivePropertyHandler() {
		super(Properties.ACTIVE);
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return avatarMapper.has(entity);
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		return observedMapper.has(entity);
	}
}
