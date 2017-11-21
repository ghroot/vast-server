package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.Profiler;

public abstract class AbstractProfiledBaseSystem extends BaseSystem {
	private Profiler profiler;

	@Override
	protected void initialize() {
		profiler = new Profiler();
		profiler.initialize(this, world);
	}

	@Override
	protected void begin() {
		profiler.start();
	}

	@Override
	protected void end() {
		profiler.stop();
	}

	@Override
	protected abstract void processSystem();
}
