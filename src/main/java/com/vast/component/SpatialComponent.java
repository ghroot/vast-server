package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;

public class SpatialComponent extends PooledComponent {
	public Point2i memberOfSpatialHash;
	public Point2f lastUsedPosition;

	@Override
	protected void reset() {
		memberOfSpatialHash = null;
		lastUsedPosition = null;
	}
}
