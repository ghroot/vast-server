package test;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	private int timePerFrameMs;
	public Map<String, Integer> systemProcessingTimes = new HashMap<String, Integer>();

	public int getTimePerFrameMs() {
		return timePerFrameMs;
	}

	public void setTimePerFrameMs(int timePerFrameMs) {
		this.timePerFrameMs = timePerFrameMs;
	}

	public int getFps() {
		return 1000 / timePerFrameMs;
	}
}
