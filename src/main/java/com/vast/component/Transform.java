package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class Transform extends PooledComponent {
	public Point2f position = new Point2f();
	public float rotation = 0.0f;

	@Override
	protected void reset() {
		position = new Point2f();
		rotation = 0.0f;
	}
}
