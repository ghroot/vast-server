package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Avatar;
import com.vast.component.Observed;
import com.vast.component.Observer;
import com.vast.network.VastPeer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDeactivateSystem {
	@Test
	public void deactivatesEntityWhenNoPeer() {
		World world = new World(new WorldConfigurationBuilder().with(
			new DeactivateSystem(new HashMap<>())
		).build());

		ComponentMapper<Avatar> avatarMapper = world.getMapper(Avatar.class);
		ComponentMapper<Observed> observedMapper = world.getMapper(Observed.class);
		ComponentMapper<Observer> observerMapper = world.getMapper(Observer.class);

		int avatarEntity = world.create();
		int observerEntity = world.create();

		Observer observer = observerMapper.create(observerEntity);
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn(123L);
		observer.peer = peer;

		avatarMapper.create(avatarEntity).name = "TestName";
		observedMapper.create(avatarEntity).observerEntity = observerEntity;
		observer.observedEntity = avatarEntity;

		world.process();

		assertFalse(observedMapper.has(avatarEntity));
		assertFalse(world.getEntityManager().isActive(observerEntity));
	}

	@Test
	public void deactivatesEntityWhenPeerWithDifferentId() {
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer1 = mock(VastPeer.class);
		when(peer1.getId()).thenReturn(123L);
		peers.put("TestName", peer1);
		World world = new World(new WorldConfigurationBuilder().with(
			new DeactivateSystem(peers)
		).build());

		ComponentMapper<Avatar> avatarMapper = world.getMapper(Avatar.class);
		ComponentMapper<Observed> observedMapper = world.getMapper(Observed.class);
		ComponentMapper<Observer> observerMapper = world.getMapper(Observer.class);

		int avatarEntity = world.create();
		int observerEntity = world.create();

		Observer observer = observerMapper.create(observerEntity);
		VastPeer peer2 = mock(VastPeer.class);
		when(peer2.getId()).thenReturn(321L);
		observer.peer = peer2;

		avatarMapper.create(avatarEntity).name = "TestName";
		observedMapper.create(avatarEntity).observerEntity = observerEntity;
		observer.observedEntity = avatarEntity;

		world.process();

		assertFalse(observedMapper.has(avatarEntity));
		assertFalse(world.getEntityManager().isActive(observerEntity));
	}
}
