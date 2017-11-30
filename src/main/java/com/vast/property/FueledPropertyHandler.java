package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Fueled;

public class FueledPropertyHandler implements PropertyHandler {
	private ComponentMapper<Fueled> fueledMapper;

	@Override
	public int getProperty() {
		return Properties.FUELED;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (fueledMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_FUELED, fueledMapper.get(entity).timeLeft > 0.0f);
			return true;
		}
		return false;
	}
}
