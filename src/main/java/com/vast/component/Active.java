package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Transient;
import com.artemis.utils.IntBag;
import com.vast.network.VastPeer;

@Transient
public class Active extends PooledComponent {
	public VastPeer peer;
	@EntityId public transient IntBag knowEntities = new IntBag();

	@Override
	protected void reset() {
		peer = null;
		knowEntities.clear();
	}
}
