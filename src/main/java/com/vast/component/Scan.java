package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.utils.IntBag;

public class Scan extends PooledComponent {
	public float distance = 10f;
	@EntityId public transient IntBag nearbyEntities = new IntBag();

	@Override
	protected void reset() {
		distance = 10f;
		nearbyEntities = new IntBag();
	}
}
