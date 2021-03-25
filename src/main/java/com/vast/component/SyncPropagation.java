package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.vast.network.Properties;

@PooledWeaver
public class SyncPropagation extends Component {
	private static final int DEFAULT_UNRELIABLE = (1 << Properties.POSITION) | (1 << Properties.ROTATION) |
			(1 << Properties.PROGRESS);
	private static final int DEFAULT_OWNER_PROPAGATION = (1 << Properties.PROGRESS) | (1 << Properties.INVENTORY) |
			(1 << Properties.HOME) | (1 << Properties.CONFIGURATION) | (1 << Properties.SKILL) | (1 << Properties.VALID);
	private static final int DEFAULT_BLOCKED = 0;

	public int unreliableProperties = DEFAULT_UNRELIABLE;
	public int ownerPropagationProperties = DEFAULT_OWNER_PROPAGATION;
	public int blockedProperties = DEFAULT_BLOCKED;

	public SyncPropagation setUnreliable(int property) {
		unreliableProperties |= (1 << property);
		return this;
	}

	public SyncPropagation setReliable(int property) {
		unreliableProperties &= ~(1 << property);
		return this;
	}

	public boolean isUnreliable(int property) {
		return (unreliableProperties & (1 << property)) > 0;
	}

	// Sends messages reliably
	public boolean isReliable(int property) {
		return !isUnreliable(property);
	}

	public SyncPropagation setOwnerPropagation(int property) {
		ownerPropagationProperties |= (1 << property);
		return this;
	}

	public SyncPropagation setNearbyPropagation(int property) {
		ownerPropagationProperties &= ~(1 << property);
		return this;
	}

	// Only visible to owner of entity
	// Owner can be:
	// - Owner component's name
	// - Avatar component's name
	// - Observed component's observer entity
	// - Observer itself
	public boolean isOwnerPropagation(int property) {
		return (ownerPropagationProperties & (1 << property)) > 0;
	}

	public boolean isNearbyPropagation(int property) {
		return !isOwnerPropagation(property);
	}

	public SyncPropagation blockAll() {
		blockedProperties = -1;
		return this;
	}

	public SyncPropagation unblock(int property) {
		blockedProperties &= ~(1 << property);
		return this;
	}

	public SyncPropagation block(int property) {
		blockedProperties |= (1 << property);
		return this;
	}

	// Is not visible to anyone
	public boolean isBlocked(int property) {
		return (blockedProperties & (1 << property)) > 0;
	}

	public SyncPropagation clear() {
		unreliableProperties = 0;
		ownerPropagationProperties = 0;
		blockedProperties = 0;
		return this;
	}
}
