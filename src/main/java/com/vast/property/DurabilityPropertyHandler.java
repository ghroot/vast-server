package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Harvestable;
import com.vast.component.SyncHistory;

public class DurabilityPropertyHandler implements PropertyHandler {
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private final float DURABILITY_THRESHOLD = 3.0f;

	@Override
	public int getProperty() {
		return Properties.DURABILITY;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (harvestableMapper.has(entity)) {
			Harvestable harvestable = harvestableMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			float durabilityDifference = Float.MAX_VALUE;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.DURABILITY)) {
				float lastSyncedDurability = (float) syncHistory.syncedValues.get(Properties.DURABILITY);
				durabilityDifference = Math.abs(lastSyncedDurability - harvestable.durability);
			}
			if (force || harvestable.durability == 0.0f || durabilityDifference >= DURABILITY_THRESHOLD) {
				dataObject.set(MessageCodes.PROPERTY_DURABILITY, (int) harvestable.durability);
				if (syncHistory != null) {
					syncHistory.syncedValues.put(Properties.DURABILITY, harvestable.durability);
				}
				return true;
			}
		}
		return false;
	}
}
