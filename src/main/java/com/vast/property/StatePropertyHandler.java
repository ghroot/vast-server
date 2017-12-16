package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Player;
import com.vast.component.State;

public class StatePropertyHandler implements PropertyHandler {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<State> stateMapper;

	@Override
	public int getProperty() {
		return Properties.STATE;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (stateMapper.has(entity)) {
			String stateName = stateMapper.get(entity).name;
			dataObject.set(MessageCodes.PROPERTY_STATE, stateName != null ? stateName : "");
			return true;
		}
		return false;
	}
}
