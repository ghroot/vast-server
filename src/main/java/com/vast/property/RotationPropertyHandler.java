package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;

public class RotationPropertyHandler implements PropertyHandler {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private final float ROTATION_THRESHOLD = 15.0f;

	public RotationPropertyHandler() {
	}

	@Override
	public int getProperty() {
		return Properties.ROTATION;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (transformMapper.has(entity)) {
			Transform transform = transformMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			float rotationDifference = Float.MAX_VALUE;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.ROTATION)) {
				float lastSyncedRotation = (float) syncHistory.syncedValues.get(Properties.ROTATION);
				rotationDifference = getAngleDifference(lastSyncedRotation, transform.rotation);
			}
			if (force || rotationDifference >= ROTATION_THRESHOLD) {
				dataObject.set(MessageCodes.PROPERTY_ROTATION, transform.rotation);
				if (syncHistory != null) {
					syncHistory.syncedValues.put(Properties.ROTATION, transform.rotation);
				}
				return true;
			}
		}
		return false;
	}

	private float getAngleDifference(float alpha, float beta) {
		float phi = Math.abs(beta - alpha) % 360;
		return phi > 180.0f ? 360.0f - phi : phi;
	}
}
