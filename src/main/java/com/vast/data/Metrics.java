package com.vast.data;

import com.artemis.BaseSystem;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.nhnent.haste.transport.QoS;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	private int timePerFrameMs;
	private Map<BaseSystem, SystemMetrics> systemMetrics = new HashMap<>();
	private int numberOfCollisionChecks;
	private int numberOfCollisions;
	private double meanOfRoundTripTime;
	private Map<Short, Map<QoS, int[]>> sentMessages = new HashMap<>();
	private Map<String, Integer> sentEvents = new HashMap<>();
	private long lastSerializeTime;
	private Map<Byte, Integer> syncedProperties = new HashMap<>();

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

	public void setSystemMetrics(BaseSystem system, int processingTime, int numberOfEntitiesInSystem) {
		systemMetrics.put(system, new SystemMetrics(processingTime, numberOfEntitiesInSystem));
	}

	public Map<BaseSystem, SystemMetrics> getSystemMetrics() {
		return systemMetrics;
	}

	public void setNumberOfCollisionChecks(int numberOfCollisionChecks) {
		this.numberOfCollisionChecks = numberOfCollisionChecks;
	}

	public int getNumberOfCollisionChecks() {
		return numberOfCollisionChecks;
	}

	public int getNumberOfCollisions() {
		return numberOfCollisions;
	}

	public void setNumberOfCollisions(int numberOfCollisions) {
		this.numberOfCollisions = numberOfCollisions;
	}

	public void setRoundTripTime(double meanOfRoundTripTime) {
		this.meanOfRoundTripTime = meanOfRoundTripTime;
	}

	public double getMeanOfRoundTripTime() {
		return meanOfRoundTripTime;
	}

	public void messageSent(EventMessage message, QoS qos) {
		Map<QoS, int[]> sentMessagesWithCode = sentMessages.get(message.getCode());
		if (sentMessagesWithCode == null) {
			sentMessagesWithCode = new HashMap<>();
			sentMessages.put(message.getCode(), sentMessagesWithCode);
		}
		if (sentMessagesWithCode.containsKey(qos)) {
			sentMessagesWithCode.put(qos, new int[] {sentMessagesWithCode.get(qos)[0] + 1, sentMessagesWithCode.get(qos)[1] + message.getDataObject().serialize().length});
		} else {
			sentMessagesWithCode.put(qos, new int[] {1, message.getDataObject().serialize().length});
		}
	}

	public Map<Short, Map<QoS, int[]>> getSentMessages() {
		return sentMessages;
	}

	public void eventSent(String type) {
		if (sentEvents.containsKey(type)) {
			sentEvents.put(type, sentEvents.get(type) + 1);
		} else {
			sentEvents.put(type, 1);
		}
	}

	public Map<String, Integer> getSentEvents() {
		return sentEvents;
	}

	public int getTimeSinceLastSerialization() {
		return (int) (System.currentTimeMillis() - lastSerializeTime);
	}

	public void setLastSerializeTime(long lastSerializeTime) {
		this.lastSerializeTime = lastSerializeTime;
	}

	public void incrementSyncedProperty(byte property) {
		if (syncedProperties.containsKey(property)) {
			syncedProperties.put(property, syncedProperties.get(property) + 1);
		} else {
			syncedProperties.put(property, 1);
		}
	}

	public Map<Byte, Integer> getSyncedProperties() {
		return syncedProperties;
	}
}
