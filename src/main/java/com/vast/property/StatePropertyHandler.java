package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Player;
import com.vast.component.State;
import com.vast.network.Properties;

public class StatePropertyHandler implements PropertyHandler {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<State> stateMapper;

	@Override
	public byte getProperty() {
		return Properties.STATE;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (stateMapper.has(entity)) {
			String stateName = stateMapper.get(entity).name;
			if (stateName != null) {
				dataObject.set(Properties.STATE, stateName);
				return true;
			}
		}
		return false;
	}
}
