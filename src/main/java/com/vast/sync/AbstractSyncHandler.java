package com.vast.sync;

import com.artemis.Aspect;

public abstract class AbstractSyncHandler implements SyncHandler {
	private Aspect.Builder aspectBuilder;

	public AbstractSyncHandler(Aspect.Builder aspectBuilder) {
		this.aspectBuilder = aspectBuilder;
	}

	@Override
	public Aspect.Builder getAspectBuilder() {
		return aspectBuilder;
	}

	@Override
	public void inserted(int entity) {
	}

	@Override
	public void removed(int entity) {
	}
}
