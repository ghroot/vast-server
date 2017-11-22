package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Building;

public class ProgressPropertyHandler implements PropertyHandler {
	private ComponentMapper<Building> buildingMapper;

	@Override
	public int getProperty() {
		return Properties.PROGRESS;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		if (buildingMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_PROGRESS, (int) buildingMapper.get(entity).progress);
		}
	}
}
