package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Inventory;

public class InventoryPropertyHandler implements PropertyHandler {
	private ComponentMapper<Inventory> inventoryMapper;

	@Override
	public int getProperty() {
		return Properties.INVENTORY;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (inventoryMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_INVENTORY, inventoryMapper.get(entity).items);
		}
	}
}
