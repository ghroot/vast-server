package com.vast.data;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class Recipe {
	private int id;

	private Set<Cost> costs;

	private int itemId = -1;
	private float duration;

	private String entityType;

	public Recipe(int id, Set<Cost> costs, int itemId, float duration) {
		this.id = id;
		this.costs = costs;
		this.itemId = itemId;
		this.duration = duration;
	}

	public Recipe(int id, Set<Cost> costs, String entityType) {
		this.id = id;
		this.costs = costs;
		this.entityType = entityType;
	}

	public int getId() {
		return id;
	}

	public Set<Cost> getCosts() {
		return costs;
	}

	public int getItemId() {
		return itemId;
	}

	public float getDuration() {
		return duration;
	}

	public String getEntityType() {
		return entityType;
	}
}
