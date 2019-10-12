package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashSet;
import java.util.Set;

public class Know extends PooledComponent {
	public transient Set<Integer> knowEntities = new HashSet<Integer>();

	@Override
	protected void reset() {
		knowEntities = new HashSet<Integer>();
	}
}
