package com.vast.component;

import com.artemis.PooledComponent;
import com.vast.SpatialHash;

import javax.vecmath.Point2f;

public class Spatial extends PooledComponent {
	public transient SpatialHash memberOfSpatialHash;
	public transient Point2f lastUsedPosition;

	@Override
	protected void reset() {
		memberOfSpatialHash = null;
		lastUsedPosition = null;
	}
}
