package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.DelayedComponentRemoval;
import com.artemis.annotations.Transient;
import com.vast.order.OrderHandler;

import static com.vast.component.Order.Type.NONE;

@Transient
@DelayedComponentRemoval
public class Order extends PooledComponent {
	public enum Type {
		NONE,
		MOVE,
		INTERACT,
		BUILD,
		EMOTE,
		SET_HOME
	}
	public Type type = NONE;

	public OrderHandler handler;

	@Override
	protected void reset() {
		type = NONE;
		handler = null;
	}
}
