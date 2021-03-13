package com.vast.property.progress;

import com.vast.network.Properties;
import com.vast.property.AbstractPropertyHandler;

public abstract class AbstractProgressPropertyHandler extends AbstractPropertyHandler<Integer, Byte> {
	private int progressThreshold;

	public AbstractProgressPropertyHandler(int progressThreshold) {
		super(Properties.PROGRESS);
		this.progressThreshold = progressThreshold;
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Integer lastSyncedProgress) {
		int progress = getPropertyData(entity);
		if (progress != lastSyncedProgress) {
			if (progress == 100) {
				return true;
			} else {
				int progressDifference = Math.abs(lastSyncedProgress - progress);
				return progressDifference >= progressThreshold;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Byte convertPropertyDataToDataObjectData(Integer propertyData) {
		return propertyData.byteValue();
	}
}
