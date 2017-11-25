package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Transform;

public class RotationPropertyHandler implements PropertyHandler {
	private ComponentMapper<Transform> transformMapper;

	public RotationPropertyHandler() {
	}

	@Override
	public int getProperty() {
		return Properties.ROTATION;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		if (transformMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_ROTATION, transformMapper.get(entity).rotation);
		}
	}
}
