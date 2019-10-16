package com.vast.data;

public class SystemMetrics {
	private int processingTime;
	private int numberOfEntitiesInSystem;

	public SystemMetrics(int processingTime, int numberOfEntitiesInSystem) {
		this.processingTime = processingTime;
		this.numberOfEntitiesInSystem = numberOfEntitiesInSystem;
	}

	public int getProcessingTime() {
		return processingTime;
	}

	public int getNumberOfEntitiesInSystem() {
		return numberOfEntitiesInSystem;
	}
}
