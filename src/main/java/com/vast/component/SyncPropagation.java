package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class SyncPropagation extends Component {
	public int unreliableProperties = 0;
	public int ownerPropagationProperties = 0;

	public void setUnreliable(int property) {
		unreliableProperties |= property;
	}

	public boolean isUnreliable(int property) {
		return (unreliableProperties & property) > 0;
	}

	public boolean isReliable(int property) {
		return !isUnreliable(property);
	}

	public void setOwnerPropagation(int property) {
		ownerPropagationProperties |= property;
	}

	public boolean isOwnerPropagation(int property) {
		return (ownerPropagationProperties & property) > 0;
	}

	public boolean isNearbyPropagation(int property) {
		return !isOwnerPropagation(property);
	}
}
