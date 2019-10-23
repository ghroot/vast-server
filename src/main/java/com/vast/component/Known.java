package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.utils.IntBag;

public class Known extends PooledComponent {
	@EntityId public transient IntBag knownByEntities = new IntBag();

	@Override
	protected void reset() {
		knownByEntities.clear();
	}
}
