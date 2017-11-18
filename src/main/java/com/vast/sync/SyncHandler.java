package com.vast.sync;

import com.artemis.Aspect;
import com.vast.VastPeer;

import java.util.Set;

public interface SyncHandler {
	Aspect.Builder getAspectBuilder();
	void inserted(int entity);
	void removed(int entity);
	boolean needsSync(int entity);
	void sync(int entity, Set<VastPeer> nearbyPeers);
}
