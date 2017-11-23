package com.vast.data;

import java.util.HashSet;
import java.util.Set;

public class Building {
	private int type;
	private String name;
	private float buildDuration;
	private Set<Cost> costs;

	public Building(int type, String name, float buildDuration) {
		this.type = type;
		this.name = name;
		this.buildDuration = buildDuration;

		costs = new HashSet<Cost>();
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public float getBuildDuration() {
		return buildDuration;
	}

	public void addCost(Cost cost) {
		costs.add(cost);
	}

	public Set<Cost> getCosts() {
		return costs;
	}
}
