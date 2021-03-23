package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.vast.order.handler.OrderHandler;

@Transient
public class Order extends PooledComponent {
	public OrderHandler handler;

	@Override
	protected void reset() {
		handler = null;
	}
}
