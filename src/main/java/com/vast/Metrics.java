package com.vast;

import com.artemis.BaseSystem;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	private int timePerFrameMs;
	private Map<String, Integer> systemProcessingTimes = new HashMap<String, Integer>();

	public int getTimePerFrameMs() {
		return timePerFrameMs;
	}

	public void setTimePerFrameMs(int timePerFrameMs) {
		this.timePerFrameMs = timePerFrameMs;
	}

	public int getFps() {
		return 1000 / timePerFrameMs;
	}

	public void setSystemProcessingTime(BaseSystem system, int processingTime) {
		systemProcessingTimes.put(system.getClass().getSimpleName(), processingTime);
	}

	public Map<String, Integer> getSystemProcessingTimes() {
		return systemProcessingTimes;
	}
}
