package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Sync extends Component {
	public transient int dirtyProperties = 0;

	public void markPropertyAsDirty(int property) {
		dirtyProperties |= property;
	}

	public boolean isPropertyDirty(int property) {
		return (dirtyProperties & property) > 0;
	}
}
