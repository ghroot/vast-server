package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Harvestable;

public class DurabilityPropertyHandler implements PropertyHandler {
	private ComponentMapper<Harvestable> harvestableMapper;

	@Override
	public int getProperty() {
		return Properties.DURABILITY;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		if (harvestableMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_DURABILITY, harvestableMapper.get(entity).durability);
		}
	}
}
