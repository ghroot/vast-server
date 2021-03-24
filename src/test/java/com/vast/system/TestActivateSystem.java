package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Avatar;
import com.vast.component.Observed;
import com.vast.component.Observer;
import com.vast.component.Sync;
import com.vast.network.Properties;
import com.vast.network.VastPeer;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestActivateSystem {
	@Test
	public void activatesEntity() {
		CreationManager creationManager = mock(CreationManager.class);
		when(creationManager.createObserver(any(), anyInt())).thenReturn(1);
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		peers.put("TestName", peer);
		World world = new World(new WorldConfigurationBuilder().with(
			creationManager,
			new ActivateSystem(peers)
		).build());

		ComponentMapper<Avatar> avatarMapper = world.getMapper(Avatar.class);
		ComponentMapper<Observed> observedMapper = world.getMapper(Observed.class);
		ComponentMapper<Observer> observerMapper = world.getMapper(Observer.class);
		ComponentMapper<Sync> syncMapper = world.getMapper(Sync.class);

		int avatarEntity = world.create();
		avatarMapper.create(avatarEntity).name = "TestName";

		world.process();

		assertTrue(observedMapper.has(avatarEntity));
		int observerEntity = observedMapper.get(avatarEntity).observerEntity;
		assertEquals(1, observerEntity);
		assertTrue(syncMapper.get(avatarEntity).isPropertyDirty(Properties.ACTIVE));
	}
}
