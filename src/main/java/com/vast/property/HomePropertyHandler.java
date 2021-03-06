package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Home;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;

import javax.vecmath.Point2f;

public class HomePropertyHandler extends AbstractPropertyHandler<Point2f, double[]> {
	private ComponentMapper<Home> homeMapper;

	private double[] reusablePosition;

	public HomePropertyHandler() {
		super(Properties.HOME);

		reusablePosition = new double[2];
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return homeMapper.has(entity);
	}

	@Override
	protected Point2f getPropertyData(int entity) {
		return homeMapper.get(entity).position;
	}

	@Override
	protected double[] convertPropertyDataToDataObjectData(Point2f position) {
		reusablePosition[0] = position.x;
		reusablePosition[1] = position.y;
		return reusablePosition;
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
