package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Transform;

public class PositionPropertyHandler implements PropertyHandler {
	private ComponentMapper<Transform> transformMapper;

	private float[] reusablePosition;

	public PositionPropertyHandler() {
		reusablePosition = new float[2];
	}

	@Override
	public int getProperty() {
		return Properties.POSITION;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		if (transformMapper.has(entity)) {
			Transform transform = transformMapper.get(entity);
			reusablePosition[0] = transform.position.x;
			reusablePosition[1] = transform.position.y;
			dataObject.set(MessageCodes.PROPERTY_POSITION, reusablePosition);
		}
	}
}
