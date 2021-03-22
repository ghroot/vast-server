package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import com.vast.property.progress.AbstractProgressPropertyHandler;
import org.junit.Test;

import java.util.HashMap;

import static org.mockito.Mockito.*;

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
			public boolean isInterestedIn(int entity) {
				return true;
			}

			@Override
			public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
				return changes;
			}
		};
	}

	private VastPeer createPeer(int id) {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getId()).thenReturn((long) id);
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

		verify(ownerPeer, times(1)).send(any());
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

		verify(ownerPeer, never()).send(any());
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

		verify(ownerPeer, never()).send(any());
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

		verify(ownerPeer, times(1)).send(any());
		verify(ownerPeer, never()).sendUnreliable(any());
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

		verify(ownerPeer, times(1)).sendUnreliable(any());
		verify(ownerPeer, never()).send(any());
	}

	@Test
	public void onlyDecoratesFirstOfEachProperty() {
		VastPeer ownerPeer = createPeer(123);
		byte property = (byte) 1;
		PropertyHandler firstPropertyHandler = mock(PropertyHandler.class);
		when(firstPropertyHandler.getProperty()).thenReturn(property);
		when(firstPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(firstPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		PropertyHandler secondPropertyHandler = mock(PropertyHandler.class);
		when(secondPropertyHandler.getProperty()).thenReturn(property);
		when(secondPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(secondPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		setupWorld(new PropertyHandler[]{firstPropertyHandler, secondPropertyHandler});

		int playerEntity = world.create();
		playerMapper.create(playerEntity);
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		syncPropagationMapper.create(playerEntity);
		syncMapper.create(playerEntity).markPropertyAsDirty(property);

		world.process();

		verify(firstPropertyHandler, times(1)).decorateDataObject(anyInt(), any(), anyBoolean());
		verify(secondPropertyHandler, never()).decorateDataObject(anyInt(), any(), anyBoolean());
	}
}
