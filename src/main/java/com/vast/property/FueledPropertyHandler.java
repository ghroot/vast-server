package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Fueled;
import com.vast.data.Properties;

public class FueledPropertyHandler implements PropertyHandler {
	private ComponentMapper<Fueled> fueledMapper;

	@Override
	public byte getProperty() {
		return Properties.FUELED;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (fueledMapper.has(entity)) {
			dataObject.set(Properties.FUELED, fueledMapper.get(entity).timeLeft > 0.0f);
			return true;
		}
		return false;
	}
}
