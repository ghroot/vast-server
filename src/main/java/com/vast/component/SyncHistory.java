package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class SyncHistory extends PooledComponent {
	public transient Map<Integer, Object> syncedValues = new HashMap<Integer, Object>();

	@Override
	protected void reset() {
		syncedValues = new HashMap<Integer, Object>();
	}
}
