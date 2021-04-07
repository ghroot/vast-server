package com.vast.data;

import java.util.Set;

public abstract class Recipe {
	private int id;
	private Set<Cost> costs;

	public Recipe(int id, Set<Cost> costs) {
		this.id = id;
		this.costs = costs;
	}

	public int getId() {
		return id;
	}

	public Set<Cost> getCosts() {
		return costs;
	}
}
