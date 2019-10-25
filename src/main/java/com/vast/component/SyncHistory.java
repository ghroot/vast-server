package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class SyncHistory extends PooledComponent {
	public transient Map<Byte, Object> syncedValues = new HashMap<>();

	@Override
	protected void reset() {
		syncedValues.clear();
	}
}
