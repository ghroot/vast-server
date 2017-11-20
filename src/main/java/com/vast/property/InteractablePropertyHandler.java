package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Interactable;

public class InteractablePropertyHandler implements PropertyHandler {
	private ComponentMapper<Interactable> interactableMapper;

	@Override
	public int getProperty() {
		return Properties.INTERACTABLE;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject) {
		dataObject.set(MessageCodes.PROPERTY_INTERACTABLE, interactableMapper.has(entity));
	}
}
