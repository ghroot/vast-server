package com.vast.sync;

import com.artemis.Aspect;
import com.vast.VastPeer;

import java.util.Set;

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

	@Override
	public abstract void sync(int entity, Set<VastPeer> nearbyPeers);
}
