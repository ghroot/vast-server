package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashSet;
import java.util.Set;

public class Known extends PooledComponent {
	public transient Set<Integer> knownByEntities = new HashSet<Integer>();

	@Override
	protected void reset() {
		knownByEntities = new HashSet<Integer>();
	}
}
