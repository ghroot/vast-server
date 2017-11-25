package com.vast;

import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;

public class ProfiledInvocationStrategy extends SystemInvocationStrategy {
	private Metrics metrics;

	public ProfiledInvocationStrategy(Metrics metrics) {
		this.metrics = metrics;
	}

	protected void process() {
		BaseSystem[] systemsData = systems.getData();
		for(int i = 0, s = systems.size(); s > i; ++i) {
			if (!disabled.get(i)) {
				updateEntityStates();
				long startTime = System.currentTimeMillis();
				systemsData[i].process();
				long endTime = System.currentTimeMillis();
				int processingTime = (int) (endTime - startTime);
				metrics.setSystemProcessingTime(systemsData[i], processingTime);
			}
		}
		updateEntityStates();
	}
}
