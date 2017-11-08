package test.system;

import com.artemis.BaseSystem;
import test.Metrics;

public class MetricsManager extends BaseSystem {
	private Metrics metrics;

	public MetricsManager(Metrics metrics) {
		this.metrics = metrics;
	}

	@Override
	protected void processSystem() {
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public void setSystemProcessingTime(BaseSystem system, int processingTime) {
		metrics.systemProcessingTimes.put(system.toString(), processingTime);
	}
}