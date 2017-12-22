package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.network.MessageCodes;
import com.vast.data.Properties;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class PositionPropertyHandler implements PropertyHandler {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private final float POSITION_THRESHOLD = 0.3f;

	private double[] reusablePosition;
	private Vector2f reusableVector;

	public PositionPropertyHandler() {
		reusablePosition = new double[2];
		reusableVector = new Vector2f();
	}

	@Override
	public byte getProperty() {
		return Properties.POSITION;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (transformMapper.has(entity)) {
			Transform transform = transformMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			float positionDifference = Float.MAX_VALUE;
			Point2f lastSyncedPosition = null;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.POSITION)) {
				lastSyncedPosition = (Point2f) syncHistory.syncedValues.get(Properties.POSITION);
				reusableVector.set(lastSyncedPosition.x - transform.position.x, lastSyncedPosition.y - transform.position.y);
				positionDifference = reusableVector.length();
			}
			if (force || positionDifference >= POSITION_THRESHOLD) {
				reusablePosition[0] = transform.position.x;
				reusablePosition[1] = transform.position.y;
				dataObject.set(Properties.POSITION, reusablePosition);
				if (syncHistory != null) {
					if (lastSyncedPosition != null) {
						lastSyncedPosition.set(transform.position);
					} else {
						syncHistory.syncedValues.put(Properties.POSITION, new Point2f(transform.position));
					}
				}
				return true;
			}
		}
		return false;
	}
}
