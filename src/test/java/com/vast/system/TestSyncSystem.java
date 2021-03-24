package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TestSyncSystem {
	private World world;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Owner> ownerMapper;

	private void setupWorld(VastPeer peer, PropertyHandler[] propertyHandlers) {
		Map<String, VastPeer> peers = new HashMap<>();
		peers.put(peer.getName(), peer);
		world = new World(new WorldConfigurationBuilder().with(
			new SyncSystem(peers, propertyHandlers, null)
		).build());

		observerMapper = world.getMapper(Observer.class);
		knownMapper = world.getMapper(Known.class);
		syncPropagationMapper = world.getMapper(SyncPropagation.class);
		syncMapper = world.getMapper(Sync.class);
		ownerMapper = world.getMapper(Owner.class);
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

	private VastPeer createPeer(String name) {
		VastPeer peer = mock(VastPeer.class);
		when(peer.getName()).thenReturn(name);
		return peer;
	}

	@Test
	public void notifiesOwnerWhenPropertiesChanged() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
			createPropertyHandler(1, true),
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		syncPropagationMapper.create(changedEntity).setOwnerPropagation(1);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);

		world.process();

		verify(ownerPeer, times(1)).send(any());
	}

	@Test
	public void notifiesNearbyWhenPropertiesChanged() {
		VastPeer nearbyPeer = createPeer("TestPeer");
		setupWorld(nearbyPeer, new PropertyHandler[]{
			createPropertyHandler(1, true),
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = nearbyPeer;

		knownMapper.create(changedEntity).knownByEntities.add(observerEntity);
		syncPropagationMapper.create(changedEntity);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);

		world.process();

		verify(nearbyPeer, times(1)).send(any());
	}

	@Test
	public void doesNotNotifyNearbyIfNotKnownWhenPropertiesChanged() {
		VastPeer nearbyPeer = createPeer("TestPeer");
		setupWorld(nearbyPeer, new PropertyHandler[]{
				createPropertyHandler(1, true),
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = nearbyPeer;

		syncPropagationMapper.create(changedEntity);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);

		world.process();

		verify(nearbyPeer, never()).send(any());
	}

	@Test
	public void notifiesOwnerOnceEvenIfSeveralPropertiesChanged() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
			createPropertyHandler(1, true),
			createPropertyHandler(2, true)
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		syncPropagationMapper.create(changedEntity).setOwnerPropagation(1);
		syncPropagationMapper.create(changedEntity).setOwnerPropagation(2);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);
		syncMapper.create(changedEntity).markPropertyAsDirty(2);

		world.process();

		verify(ownerPeer, times(1)).send(any());
	}

	@Test
	public void doesNotNotifyOwnerIfPropertyDidNotChange() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
			createPropertyHandler(1, false)
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		syncPropagationMapper.create(changedEntity).setOwnerPropagation(1);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);

		world.process();

		verify(ownerPeer, never()).send(any());
	}

	@Test
	public void doesNotNotifyOwnerIfPropertyWasNotMarkedAsDirty() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
				createPropertyHandler(1, false)
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		syncPropagationMapper.create(changedEntity).setOwnerPropagation(1);

		world.process();

		verify(ownerPeer, never()).send(any());
	}

	@Test
	public void notifiesWithReliableWhenPropertiesHaveMixedReliability() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
				createPropertyHandler(1, true),
				createPropertyHandler(2, true)
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		knownMapper.create(changedEntity).knownByEntities.add(observerEntity);
		syncPropagationMapper.create(changedEntity).setUnreliable(2);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);
		syncMapper.create(changedEntity).markPropertyAsDirty(2);

		world.process();

		verify(ownerPeer, times(1)).send(any());
		verify(ownerPeer, never()).sendUnreliable(any());
	}

	@Test
	public void notifiesWithUnreliableWhenAllPropertiesAreUnreliable() {
		VastPeer ownerPeer = createPeer("TestPeer");
		setupWorld(ownerPeer, new PropertyHandler[]{
				createPropertyHandler(1, true),
				createPropertyHandler(2, true)
		});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		knownMapper.create(changedEntity).knownByEntities.add(observerEntity);
		syncPropagationMapper.create(changedEntity).setUnreliable(1);
		syncPropagationMapper.create(changedEntity).setUnreliable(2);
		syncMapper.create(changedEntity).markPropertyAsDirty(1);
		syncMapper.create(changedEntity).markPropertyAsDirty(2);

		world.process();

		verify(ownerPeer, times(1)).sendUnreliable(any());
		verify(ownerPeer, never()).send(any());
	}

	@Test
	public void onlyDecoratesFirstOfEachProperty() {
		VastPeer ownerPeer = createPeer("TestPeer");
		byte property = (byte) 1;
		PropertyHandler firstPropertyHandler = mock(PropertyHandler.class);
		when(firstPropertyHandler.getProperty()).thenReturn(property);
		when(firstPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(firstPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		PropertyHandler secondPropertyHandler = mock(PropertyHandler.class);
		when(secondPropertyHandler.getProperty()).thenReturn(property);
		when(secondPropertyHandler.isInterestedIn(anyInt())).thenReturn(true);
		when(secondPropertyHandler.decorateDataObject(anyInt(), any(), anyBoolean())).thenReturn(true);
		setupWorld(ownerPeer, new PropertyHandler[]{firstPropertyHandler, secondPropertyHandler});

		int observerEntity = world.create();
		int changedEntity = world.create();

		observerMapper.create(observerEntity).peer = ownerPeer;

		ownerMapper.create(changedEntity).name = "TestPeer";
		knownMapper.create(changedEntity).knownByEntities.add(observerEntity);
		syncPropagationMapper.create(changedEntity);
		syncMapper.create(changedEntity).markPropertyAsDirty(property);

		world.process();

		verify(firstPropertyHandler, times(1)).decorateDataObject(anyInt(), any(), anyBoolean());
		verify(secondPropertyHandler, never()).decorateDataObject(anyInt(), any(), anyBoolean());
	}
}
