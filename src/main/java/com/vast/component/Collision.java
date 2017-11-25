package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class Collision extends PooledComponent {
	public float radius = 0.2f;
	public transient Point2f lastCheckedPosition;

	@Override
	protected void reset() {
		radius = 0.2f;
		lastCheckedPosition = null;
	}
}
