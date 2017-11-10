package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.Metrics;

public class MetricsManager extends BaseSystem {
	private Metrics metrics;

	public MetricsManager(Metrics metrics) {
		this.metrics = metrics;
	}

	@Override
	protected void processSystem() {
	}

	public void setSystemProcessingTime(BaseSystem system, int processingTime) {
		metrics.setSystemProcessingTime(system, processingTime);
	}
}
