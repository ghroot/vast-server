package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class SyncHistory extends PooledComponent {
	// TODO: Needs to be based on with which entity each value was synced as well!
	public transient Map<Byte, Object> syncedValues = new HashMap<>();

	@Override
	protected void reset() {
		syncedValues.clear();
	}
}
