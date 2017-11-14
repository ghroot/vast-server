package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class Collision extends PooledComponent {
	public boolean isStatic = false;
	public float radius = 0.2f;
	public transient Point2f lastCheckedPosition;

	@Override
	protected void reset() {
		isStatic = false;
		radius = 0.2f;
		lastCheckedPosition = null;
	}
}
