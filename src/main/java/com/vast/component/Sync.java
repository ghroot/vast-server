package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Sync extends Component {
	public int dirtyProperties = 0;

	public void markPropertyAsDirty(int property) {
		dirtyProperties |= (1 << property);
	}

	public boolean isPropertyDirty(int property) {
		return (dirtyProperties & (1 << property)) > 0;
	}
}
