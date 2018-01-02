package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;

import javax.vecmath.Point2f;

@Transient
public class Path extends PooledComponent {
	public Point2f targetPosition = new Point2f();

	@Override
	protected void reset() {
		targetPosition = new Point2f();
	}
}
