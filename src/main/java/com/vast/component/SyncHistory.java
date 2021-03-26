package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class SyncHistory extends PooledComponent {
	public transient Map<Integer, Map<Byte, Object>> syncedValues = new HashMap<>();

	@Override
	protected void reset() {
		syncedValues.clear();
	}

	public boolean hasSyncedPropertyData(int propertyEntity, byte property) {
		return syncedValues.containsKey(propertyEntity) && syncedValues.get(propertyEntity).containsKey(property);
	}

	public Object getSyncedPropertyData(int propertyEntity, byte property) {
		if (syncedValues.containsKey(propertyEntity)) {
			Map<Byte, Object> syncedValuesForEntity = syncedValues.get(propertyEntity);
			if (syncedValuesForEntity.containsKey(property)) {
				return syncedValuesForEntity.get(property);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void removeSyncedPropertyData(int propertyEntity) {
		syncedValues.remove(propertyEntity);
	}
}
