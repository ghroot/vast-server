package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.DelayedComponentRemoval;
import com.vast.SpatialHash;

import javax.vecmath.Point2f;

@DelayedComponentRemoval
public class Spatial extends PooledComponent {
	public transient SpatialHash memberOfSpatialHash;
	public transient Point2f lastUsedPosition;

	@Override
	protected void reset() {
		memberOfSpatialHash = null;
		lastUsedPosition = null;
	}
}
