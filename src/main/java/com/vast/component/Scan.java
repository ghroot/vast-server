package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashSet;
import java.util.Set;

public class Scan extends PooledComponent {
	public float distance = 10.0f;
	public transient Set<Integer> nearbyEntities = new HashSet<Integer>();

	@Override
	protected void reset() {
		distance = 8.0f;
		nearbyEntities = new HashSet<Integer>();
	}
}
