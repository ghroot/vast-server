package com.vast.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class SyncTransformComponent extends PooledComponent {
	public Point2f lastSyncedPosition = new Point2f();

	@Override
	protected void reset() {
		lastSyncedPosition = new Point2f();
	}
}
