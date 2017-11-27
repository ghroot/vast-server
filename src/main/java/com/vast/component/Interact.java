package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.DelayedComponentRemoval;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Transient;
import com.vast.interact.InteractionHandler;

@Transient
@DelayedComponentRemoval
public class Interact extends PooledComponent {
	@EntityId public int entity = -1;

	public enum Phase {
		NONE,
		APPROACHING,
		INTERACTING
	}
	public Phase phase = Phase.NONE;

	public InteractionHandler handler = null;

	@Override
	protected void reset() {
		entity = -1;
		phase = Phase.NONE;
		handler = null;
	}
}
