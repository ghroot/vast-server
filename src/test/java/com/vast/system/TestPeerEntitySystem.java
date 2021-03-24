package com.vast.system;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Avatar;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestPeerEntitySystem {
	@Test
	public void createsNewPeer() {
		CreationManager creationManager = mock(CreationManager.class);
		when(creationManager.createAvatar(anyString(), anyInt())).thenReturn(1);
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

	@Test
	public void connectsExistingPeer() {
		CreationManager creationManager = mock(CreationManager.class);
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

		int avatarEntity = world.create();
		world.getMapper(Avatar.class).create(avatarEntity).name = "TestName";

		world.process();

		verify(creationManager, never()).createAvatar(anyString(), anyInt());
		Assert.assertEquals(1, entitiesByPeer.size());
		Assert.assertEquals(avatarEntity, entitiesByPeer.get("TestName").intValue());
	}
}
