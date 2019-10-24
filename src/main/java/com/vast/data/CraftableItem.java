package com.vast.data;

import java.util.Set;

public class CraftableItem extends Item {
	private Set<Cost> costs;
	private float craftDuration;

	public CraftableItem(int id, String[] tags, String name, Set<Cost> costs, float craftDuration) {
		super(id, tags, name);
		this.costs = costs;
		this.craftDuration = craftDuration;
	}

	public Set<Cost> getCosts() {
		return costs;
	}

	public float getCraftDuration() {
		return craftDuration;
	}
}
