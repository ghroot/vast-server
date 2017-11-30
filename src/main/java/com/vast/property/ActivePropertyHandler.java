package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Active;
import com.vast.component.Player;

public class ActivePropertyHandler implements PropertyHandler {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	@Override
	public int getProperty() {
		return Properties.ACTIVE;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (playerMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_ACTIVE, activeMapper.has(entity));
			return true;
		}
		return false;
	}
}
