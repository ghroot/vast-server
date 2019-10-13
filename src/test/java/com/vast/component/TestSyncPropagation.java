package com.vast.component;

import org.junit.Assert;
import org.junit.Test;

public class TestSyncPropagation {
	@Test
	public void defaultsToReliable() {
		SyncPropagation syncPropagation = new SyncPropagation();

		Assert.assertTrue(syncPropagation.isReliable(0));
	}

	@Test
	public void defaultsToNearbyPropagation() {
		SyncPropagation syncPropagation = new SyncPropagation();

		Assert.assertTrue(syncPropagation.isNearbyPropagation(0));
	}
}
