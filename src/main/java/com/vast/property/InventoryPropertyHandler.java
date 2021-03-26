package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Inventory;
import com.vast.network.Properties;

import java.util.Arrays;

public class InventoryPropertyHandler extends AbstractPropertyHandler<short[], short[]> {
	private ComponentMapper<Inventory> inventoryMapper;

	public InventoryPropertyHandler() {
		super(Properties.INVENTORY);
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return inventoryMapper.has(entity);
	}

	@Override
	protected short[] getPropertyData(int entity) {
		return inventoryMapper.get(entity).items;
	}

	protected boolean passedThresholdForSync(int entity, short[] lastSyncedInventory) {
		return !Arrays.equals(inventoryMapper.get(entity).items, lastSyncedInventory);
	}

	@Override
	protected void setSyncHistoryData(int interestedEntity, int propertyEntity, short[] items) {
		super.setSyncHistoryData(interestedEntity, propertyEntity, Arrays.copyOf(items, items.length));
	}
}
