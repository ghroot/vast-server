package com.vast;

import com.artemis.BaseEntitySystem;
import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;
import com.vast.data.Metrics;

public class ProfiledInvocationStrategy extends SystemInvocationStrategy {
	private Metrics metrics;

	public ProfiledInvocationStrategy(Metrics metrics) {
		this.metrics = metrics;
	}

	protected void process() {
		BaseSystem[] systemsData = systems.getData();
		for (int i = 0, s = systems.size(); s > i; ++i) {
			if (!disabled.get(i)) {
				BaseSystem system = systemsData[i];
				updateEntityStates();
				int numberOfEntitiesInSystem;
				if (system instanceof BaseEntitySystem) {
					numberOfEntitiesInSystem = ((BaseEntitySystem) system).getSubscription().getEntities().size();
				} else {
					numberOfEntitiesInSystem = -1;
				}
				long startTime = System.currentTimeMillis();
				system.process();
				long endTime = System.currentTimeMillis();
				int processingTime = (int) (endTime - startTime);
				metrics.setSystemMetrics(systemsData[i], processingTime, numberOfEntitiesInSystem);
			}
		}
		updateEntityStates();
	}
}
