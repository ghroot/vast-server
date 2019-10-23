package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class Home extends PooledComponent {
	public Point2f position = new Point2f();

	@Override
	protected void reset() {
		position.set(0f, 0f);
	}
}
