package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashSet;
import java.util.Set;

public class Known extends PooledComponent {
	public transient Set<Integer> knownEntities = new HashSet<Integer>();

	@Override
	protected void reset() {
		knownEntities = new HashSet<Integer>();
	}
}
