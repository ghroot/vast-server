package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Constructable;

public class ProgressPropertyHandler implements PropertyHandler {
	private ComponentMapper<Constructable> constructableMapper;

	@Override
	public int getProperty() {
		return Properties.PROGRESS;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		if (constructableMapper.has(entity)) {
			Constructable constructable = constructableMapper.get(entity);
			int progress = (int) Math.floor(100.0f * constructable.buildTime / constructable.buildDuration);
			dataObject.set(MessageCodes.PROPERTY_PROGRESS, progress);
		}
	}
}
