package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Inventory;
import com.vast.component.SyncHistory;

import java.util.Arrays;

public class InventoryPropertyHandler implements PropertyHandler {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	@Override
	public int getProperty() {
		return Properties.INVENTORY;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (inventoryMapper.has(entity)) {
			Inventory inventory = inventoryMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			boolean inventoryChanged = false;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.INVENTORY)) {
				int lastSyncedItemsHashCode = (int) syncHistory.syncedValues.get(Properties.INVENTORY);
				inventoryChanged = Arrays.hashCode(inventory.items) != lastSyncedItemsHashCode;
			}
			if (force || inventoryChanged) {
				dataObject.set(MessageCodes.PROPERTY_INVENTORY, inventory.items);
				if (syncHistory != null) {
					syncHistory.syncedValues.put(Properties.INVENTORY, Arrays.hashCode(inventory.items));
				}
				return true;
			}
		}
		return false;
	}
}
