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
}
