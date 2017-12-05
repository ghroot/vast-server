package com.vast.data;

import java.util.HashSet;
import java.util.Set;

public class Item {
	private int type;
	private String name;
	private Set<Cost> costs;

	public Item(int type, String name) {
		this.type = type;
		this.name = name;

		costs = new HashSet<Cost>();
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void addCost(Cost cost) {
		costs.add(cost);
	}

	public Set<Cost> getCosts() {
		return costs;
	}
}
