package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class SyncPropagation extends PooledComponent {
	public static final boolean DEFAULT_RELIABLE = true;
	public Map<Integer, Boolean> reliableByProperty = new HashMap<Integer, Boolean>();

	public enum Propagation {
		NEARBY,
		OWNER;
	}
	public static final Propagation DEFAULT_PROPAGATION = Propagation.NEARBY;
	public Map<Integer, Propagation> propagationByProperty = new HashMap<Integer, Propagation>();

	@Override
	protected void reset() {
		reliableByProperty = new HashMap<Integer, Boolean>();
		propagationByProperty = new HashMap<Integer, Propagation>();
	}

	public void setReliable(int property, boolean reliable) {
		reliableByProperty.put(property, reliable);
	}

	public boolean getReliable(int property) {
		if (reliableByProperty.containsKey(property)) {
			return reliableByProperty.get(property);
		} else {
			return DEFAULT_RELIABLE;
		}
	}

	public void setPropagation(int property, Propagation propagation) {
		propagationByProperty.put(property, propagation);
	}

	public Propagation getPropagation(int property) {
		if (propagationByProperty.containsKey(property)) {
			return propagationByProperty.get(property);
		} else {
			return DEFAULT_PROPAGATION;
		}
	}
}
