package test;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	public int timePerFrameMs;
	public int fps;
	public Map<String, Integer> systemProcessingTimes = new HashMap<String, Integer>();
}
