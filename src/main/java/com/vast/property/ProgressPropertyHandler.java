package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Constructable;
import com.vast.network.Properties;

public class ProgressPropertyHandler extends AbstractPropertyHandler<Integer, Byte> {
	private ComponentMapper<Constructable> constructableMapper;

	private int progressThreshold;

	public ProgressPropertyHandler(int progressThreshold) {
		super(Properties.PROGRESS);

		this.progressThreshold = progressThreshold;
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return constructableMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		Constructable constructable = constructableMapper.get(entity);
		return Math.min((int) Math.floor(100.0f * constructable.buildTime / constructable.buildDuration), 100);
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
}
