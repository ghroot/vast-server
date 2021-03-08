package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class TestSyncSystem {
	private World world;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Sync> syncMapper;

	private void setupWorld(PropertyHandler[] propertyHandlers) {
		world = new World(new WorldConfigurationBuilder().with(
			new SyncSystem(new HashMap<>(), propertyHandlers, null)
		).build());

		playerMapper = world.getMapper(Player.class);
		activeMapper = world.getMapper(Active.class);
		knownMapper = world.getMapper(Known.class);
		syncPropagationMapper = world.getMapper(SyncPropagation.class);
		syncMapper = world.getMapper(Sync.class);
	}

	private PropertyHandler createPropertyHandler(int property, boolean changes) {
		return new PropertyHandler() {
			@Override
			public byte getProperty() {
				return (byte) property;
			}

			@Override
			public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
				return changes;
			}
		};
	}

	private VastPeer createPeer(int id) {
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn((long) id);
		return peer;
	}

	@Test
	public void notifiesOwnerOnceEvenIfSeveralPropertiesChanged() {
		VastPeer ownerPeer = createPeer(123);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, true),
			createPropertyHandler(2, true)
		});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(1);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(2);
		syncMapper.create(playerEntity).markPropertyAsDirty(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(2);

		world.process();

		Mockito.verify(ownerPeer, Mockito.times(1)).send(Mockito.any());
	}

	@Test
	public void doesNotNotifyOwnerIfPropertyDidNotChange() {
		VastPeer ownerPeer = createPeer(123);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, false)
		});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(1);

		world.process();

		Mockito.verify(ownerPeer, Mockito.never()).send(Mockito.any());
	}

	@Test
	public void doesNotNotifyOwnerIfPropertyWasNotMarkedAsDirty() {
		VastPeer ownerPeer = createPeer(123);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, true)
		});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(1);

		world.process();

		Mockito.verify(ownerPeer, Mockito.never()).send(Mockito.any());
	}

	@Test
	public void notifiesWithReliableWhenPropertiesHaveMixedReliability() {
		VastPeer ownerPeer = createPeer(123);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, true),
			createPropertyHandler(2, true)
		});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity).setUnreliable(2);
		syncMapper.create(playerEntity).markPropertyAsDirty(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(2);

		world.process();

		Mockito.verify(ownerPeer, Mockito.times(1)).send(Mockito.any());
		Mockito.verify(ownerPeer, Mockito.never()).sendUnreliable(Mockito.any());
	}

	@Test
	public void notifiesWithUnreliableWhenAllPropertiesAreUnreliable() {
		VastPeer ownerPeer = createPeer(123);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, true),
			createPropertyHandler(2, true)
		});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity).setUnreliable(1);
		syncPropagationMapper.create(playerEntity).setUnreliable(2);
		syncMapper.create(playerEntity).markPropertyAsDirty(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(2);

		world.process();

		Mockito.verify(ownerPeer, Mockito.times(1)).sendUnreliable(Mockito.any());
		Mockito.verify(ownerPeer, Mockito.never()).send(Mockito.any());
	}

	@Test
	public void notifiesOwnerAndNearbyButNotFarPlayer() {
		VastPeer ownerPeer = createPeer(123);
		VastPeer nearbyPeer = createPeer(321);
		VastPeer farPeer = createPeer(213);
		setupWorld(new PropertyHandler[]{
			createPropertyHandler(1, true),
			createPropertyHandler(2, true)
		});

		int playerEntity = world.create();
		int nearbyEntity = world.create();
		int farEntity = world.create();

		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		knownMapper.get(playerEntity).knownByEntities.add(nearbyEntity);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(1);
		syncMapper.create(playerEntity).markPropertyAsDirty(2);

		playerMapper.create(nearbyEntity);
		activeMapper.create(nearbyEntity).peer = nearbyPeer;

		playerMapper.create(farEntity);
		activeMapper.create(farEntity).peer = farPeer;

		world.process();

		Mockito.verify(ownerPeer, Mockito.times(2)).send(Mockito.any());
		Mockito.verify(nearbyPeer, Mockito.times(1)).send(Mockito.any());
		Mockito.verify(farPeer, Mockito.never()).send(Mockito.any());
	}
}
