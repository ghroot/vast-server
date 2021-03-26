package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.DelayedComponentRemoval;
import com.artemis.annotations.Transient;
import com.vast.order.handler.OrderHandler;

@DelayedComponentRemoval
@Transient
public class Order extends PooledComponent {
	public OrderHandler handler;

	@Override
	protected void reset() {
		handler = null;
	}
}
