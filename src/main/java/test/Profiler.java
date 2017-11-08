package test;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.artemis.utils.ArtemisProfiler;
import test.system.MetricsManager;

public class Profiler implements ArtemisProfiler {
	private BaseSystem system;
	private MetricsManager metricsManager;
	private long startTime;

	@Override
	public void initialize(BaseSystem system, World world) {
		this.system = system;
		metricsManager = world.getSystem(MetricsManager.class);
	}

	@Override
	public void start() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public void stop() {
		long endTime = System.currentTimeMillis();
		int processingTime = (int) (endTime - startTime);
		metricsManager.setSystemProcessingTime(system, processingTime);
	}
}
