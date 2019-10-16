package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.utils.IntBag;

public class Know extends PooledComponent {
	@EntityId
	public transient IntBag knowEntities = new IntBag();

	@Override
	protected void reset() {
		knowEntities = new IntBag();
	}
}
