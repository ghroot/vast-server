package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.network.Properties;

// TODO: Is this property handler even needed? The client doesn't listen for active property changes!
public class ActivePropertyHandler extends AbstractPropertyHandler<Boolean, Boolean> {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	public ActivePropertyHandler() {
		super(Properties.ACTIVE);
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return playerMapper.has(entity);
	}

	@Override
	protected Boolean getPropertyData(int entity) {
		return activeMapper.has(entity);
	}
}
