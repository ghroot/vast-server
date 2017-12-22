package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Growing;
import com.vast.data.Properties;

public class GrowingPropertyHandler implements PropertyHandler {
	private ComponentMapper<Growing> growingMapper;

	@Override
	public byte getProperty() {
		return Properties.GROWING;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (force) {
			if (growingMapper.has(entity)) {
				dataObject.set(Properties.GROWING, growingMapper.get(entity).timeLeft > 0.0f);
				return true;
			}
			return false;
		} else {
			dataObject.set(Properties.GROWING, growingMapper.has(entity) && growingMapper.get(entity).timeLeft > 0.0f);
			return true;
		}
	}
}
