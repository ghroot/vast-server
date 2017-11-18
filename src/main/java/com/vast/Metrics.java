package com.vast;

import com.artemis.BaseSystem;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	private int timePerFrameMs;
	private Map<String, Integer> systemProcessingTimes = new HashMap<String, Integer>();
	private int numberOfCollisionChecks;
	private double meanOfRoundTripTime;
	private double meanOfRoundTripTimeDeviation;
	private int numberOfSentMessages;
	private long lastSerializeTime;

	public int getTimePerFrameMs() {
		return timePerFrameMs;
	}

	public void setTimePerFrameMs(int timePerFrameMs) {
		this.timePerFrameMs = timePerFrameMs;
	}

	public int getFps() {
		if (timePerFrameMs > 0){
			return 1000 / timePerFrameMs;
		} else {
			return 0;
		}
	}

	public void setSystemProcessingTime(BaseSystem system, int processingTime) {
		systemProcessingTimes.put(system.getClass().getSimpleName(), processingTime);
	}

	public Map<String, Integer> getSystemProcessingTimes() {
		return systemProcessingTimes;
	}

	public void setNumberOfCollisionChecks(int numberOfCollisionChecks) {
		this.numberOfCollisionChecks = numberOfCollisionChecks;
	}

	public int getNumberOfCollisionChecks() {
		return numberOfCollisionChecks;
	}

	public void setRoundTripTime(double meanOfRoundTripTime, double meanOfRoundTripTimeDeviation) {

		this.meanOfRoundTripTime = meanOfRoundTripTime;
		this.meanOfRoundTripTimeDeviation = meanOfRoundTripTimeDeviation;
	}

	public double getMeanOfRoundTripTime() {
		return meanOfRoundTripTime;
	}

	public double getMeanOfRoundTripTimeDeviation() {
		return meanOfRoundTripTimeDeviation;
	}

	public int getNumberOfSentMessages() {
		return numberOfSentMessages;
	}

	public void incrementNumberOfSentMessages() {
		this.numberOfSentMessages++;
	}

	public int getTimeSinceLastSerialization() {
		return (int) (System.currentTimeMillis() - lastSerializeTime);
	}

	public void setLastSerializeTime(long lastSerializeTime) {
		this.lastSerializeTime = lastSerializeTime;
	}
}
