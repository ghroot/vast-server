package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Active;
import com.vast.network.Properties;

public class ActivePropertyHandler implements PropertyHandler {
	private ComponentMapper<Active> activeMapper;

	@Override
	public byte getProperty() {
		return Properties.ACTIVE;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		return activeMapper.has(entity);
	}
}
