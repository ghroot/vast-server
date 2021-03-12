package com.vast.property.progress;

import com.vast.network.Properties;
import com.vast.property.AbstractPropertyHandler;

// TODO: There is a big issue currently if one entity has several types of progresses at the same time, for example
//  constructing a planter.
//  Solutions include:
//  1) Combine all progress property handlers into one again
//  2) Make the building entity while under construction not have Plantable etc. In essence this means turning the
//  building entities into some kind of palceholders while build as well.
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
