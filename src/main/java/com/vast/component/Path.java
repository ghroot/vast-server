package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;

import javax.vecmath.Point2f;

@Transient
public class Path extends PooledComponent {
	public Point2f targetPosition = new Point2f();
	public Point2f stuckCheckPosition = new Point2f();
	public float timeSinceLastStuckCheck;
	public float timeInSamePosition = 0f;

	@Override
	protected void reset() {
		targetPosition.set(0f, 0f);
		stuckCheckPosition.set(0f, 0f);
		timeSinceLastStuckCheck = 0f;
		timeInSamePosition = 0f;
	}
}
