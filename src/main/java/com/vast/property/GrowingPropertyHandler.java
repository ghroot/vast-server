package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Growing;

public class GrowingPropertyHandler implements PropertyHandler {
	private ComponentMapper<Growing> growingMapper;

	@Override
	public int getProperty() {
		return Properties.GROWING;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (force) {
			if (growingMapper.has(entity)) {
				dataObject.set(MessageCodes.PROPERTY_GROWING, growingMapper.get(entity).timeLeft > 0.0f);
				return true;
			}
			return false;
		} else {
			dataObject.set(MessageCodes.PROPERTY_GROWING, growingMapper.has(entity) && growingMapper.get(entity).timeLeft > 0.0f);
			return true;
		}
	}
}
