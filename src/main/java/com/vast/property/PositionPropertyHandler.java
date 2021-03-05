package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class PositionPropertyHandler extends AbstractPropertyHandler<Point2f, double[]> {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private float distanceThreshold2;

	private double[] reusablePosition;
	private Vector2f reusableVector;

	public PositionPropertyHandler(float distanceThreshold) {
		super(Properties.POSITION);

		distanceThreshold2 = distanceThreshold * distanceThreshold;

		reusablePosition = new double[2];
		reusableVector = new Vector2f();
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return transformMapper.has(entity);
	}

	@Override
	protected Point2f getPropertyData(int entity) {
		return transformMapper.get(entity).position;
	}

	@Override
	protected double[] convertPropertyDataToDataObjectData(Point2f position) {
		reusablePosition[0] = position.x;
		reusablePosition[1] = position.y;
		return reusablePosition;
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Point2f lastSyncedPosition) {
		Transform transform = transformMapper.get(entity);
		reusableVector.set(lastSyncedPosition.x - transform.position.x, lastSyncedPosition.y - transform.position.y);
		return reusableVector.lengthSquared() >= distanceThreshold2;
	}

	@Override
	protected void setSyncHistoryData(int entity, Point2f position) {
		SyncHistory syncHistory = syncHistoryMapper.get(entity);
		if (syncHistory != null) {
			Point2f lastSyncedPosition = getSyncHistoryData(entity);
			if (lastSyncedPosition != null) {
				lastSyncedPosition.set(position);
			} else {
				super.setSyncHistoryData(entity, new Point2f(position));
			}
		}
	}
}
