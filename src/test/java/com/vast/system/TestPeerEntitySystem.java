package com.vast.system;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestPeerEntitySystem {
	@Test
	public void addsPeer() {
		CreationManager creationManager = mock(CreationManager.class);
		when(creationManager.createPlayer(anyString(), anyInt(), anyBoolean())).thenReturn(1);
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = mock(VastPeer.class);
		when(peer.getName()).thenReturn("TestName");
		peers.put("TestName", peer);
		Map<String, Integer> entitiesByPeer = new HashMap<>();
		PeerEntitySystem peerEntitySystem = new PeerEntitySystem(peers, entitiesByPeer);

		World world = new World(new WorldConfigurationBuilder().with(
			creationManager,
			peerEntitySystem
		).build());

		world.process();

		Assert.assertEquals(1, entitiesByPeer.size());
		Assert.assertEquals(1, entitiesByPeer.get("TestName").intValue());
	}
}
