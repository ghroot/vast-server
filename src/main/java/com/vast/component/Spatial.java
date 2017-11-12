package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;

public class Spatial extends PooledComponent {
	public transient Point2i memberOfSpatialHash;
	public transient Point2f lastUsedPosition;

	@Override
	protected void reset() {
		memberOfSpatialHash = null;
		lastUsedPosition = null;
	}
}
