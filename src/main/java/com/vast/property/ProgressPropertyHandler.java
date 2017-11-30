package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Constructable;
import com.vast.component.SyncHistory;

public class ProgressPropertyHandler implements PropertyHandler {
	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private final int PROGRESS_THRESHOLD = 3;

	@Override
	public int getProperty() {
		return Properties.PROGRESS;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (constructableMapper.has(entity)) {
			Constructable constructable = constructableMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			int progress = (int) Math.floor(100.0f * constructable.buildTime / constructable.buildDuration);

			int progressDifference = Integer.MAX_VALUE;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.PROGRESS)) {
				int lastSyncedProgress = (int) syncHistory.syncedValues.get(Properties.PROGRESS);
				progressDifference = (int) Math.abs(lastSyncedProgress - progress);
			}
			if (force || progress == 100 || progressDifference >= PROGRESS_THRESHOLD) {
				dataObject.set(MessageCodes.PROPERTY_PROGRESS, progress);
				if (syncHistory != null) {
					syncHistory.syncedValues.put(Properties.PROGRESS, progress);
				}
				return true;
			}
		}
		return false;
	}
}
