package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;

// TODO: Add ability to hve more than one event name/data/ownerOnly in the same update
@Transient
public class Event extends PooledComponent {
	public String type = null;
	public Object data = null;
	public boolean ownerOnly = false;

	@Override
	protected void reset() {
		type = null;
		data = null;
		ownerOnly = false;
	}

	public Event setType(String type) {
		this.type = type;
		return this;
	}

	public Event setData(Object data) {
		this.data = data;
		return this;
	}

	public Event setOwnerOnly(boolean ownerOnly) {
		this.ownerOnly = ownerOnly;
		return this;
	}
}
