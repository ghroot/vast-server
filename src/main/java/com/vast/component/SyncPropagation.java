package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class SyncPropagation extends Component {
	public int unreliableProperties = 0;
	public int ownerPropagationProperties = 0; // TODO: Consider concept of "nearby owners" (Owner component)

	public void setUnreliable(int property) {
		unreliableProperties |= (1 << property);
	}

	public boolean isUnreliable(int property) {
		return (unreliableProperties & (1 << property)) > 0;
	}

	public boolean isReliable(int property) {
		return !isUnreliable(property);
	}

	public void setOwnerPropagation(int property) {
		ownerPropagationProperties |= (1 << property);
	}

	public boolean isOwnerPropagation(int property) {
		return (ownerPropagationProperties & (1 << property)) > 0;
	}

	public boolean isNearbyPropagation(int property) {
		return !isOwnerPropagation(property);
	}
}
