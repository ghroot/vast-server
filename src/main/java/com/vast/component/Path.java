package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;

import javax.vecmath.Point2f;

@Transient
public class Path extends PooledComponent {
	public Point2f targetPosition = new Point2f();
	public Point2f lastPosition = null;
	public float timeInSamePosition = 0f;

	@Override
	protected void reset() {
		targetPosition.set(0f, 0f);
		lastPosition = null;
		timeInSamePosition = 0f;
	}
}
