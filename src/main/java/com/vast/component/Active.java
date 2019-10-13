package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.vast.network.VastPeer;

@Transient
public class Active extends PooledComponent {
	public VastPeer peer;

	@Override
	protected void reset() {
		peer = null;
	}
}
