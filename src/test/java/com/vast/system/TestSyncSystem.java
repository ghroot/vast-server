package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

public class TestSyncSystem {
	private final byte TEST_PROPERTY_WITH_NEARBY_PROPAGATION = 1;
	private final byte TEST_PROPERTY_WITH_OWNER_PROPAGATION = 2;

	@Test
	public void notifiesOwnerAndNearbyPlayer() {
		Set<PropertyHandler> propertyHandlers = new HashSet<PropertyHandler>();
		propertyHandlers.add(new PropertyHandler() {
			@Override
			public byte getProperty() {
				return TEST_PROPERTY_WITH_NEARBY_PROPAGATION;
			}

			@Override
			public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
				return true;
			}
		});
		propertyHandlers.add(new PropertyHandler() {
			@Override
			public byte getProperty() {
				return TEST_PROPERTY_WITH_OWNER_PROPAGATION;
			}

			@Override
			public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
				return true;
			}
		});

		VastPeer ownerPeer = Mockito.mock(VastPeer.class);
		Mockito.when(ownerPeer.getId()).thenReturn(123L);

		VastPeer nearbyPeer = Mockito.mock(VastPeer.class);
		Mockito.when(nearbyPeer.getId()).thenReturn(321L);

		World world = new World(new WorldConfigurationBuilder().with(
			new SyncSystem(propertyHandlers, null)
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);
		ComponentMapper<SyncPropagation> syncPropagationMapper = world.getMapper(SyncPropagation.class);
		ComponentMapper<Sync> syncMapper = world.getMapper(Sync.class);

		int playerEntity = world.create();
		int nearbyEntity = world.create();

		playerMapper.create(playerEntity).name = "Owner";
		activeMapper.create(playerEntity).peer = ownerPeer;
		knownMapper.create(playerEntity).knownByEntities.add(playerEntity);
		knownMapper.get(playerEntity).knownByEntities.add(nearbyEntity);
		syncPropagationMapper.create(playerEntity).setOwnerPropagation(TEST_PROPERTY_WITH_OWNER_PROPAGATION);
		syncMapper.create(playerEntity).markPropertyAsDirty(TEST_PROPERTY_WITH_NEARBY_PROPAGATION);
		syncMapper.create(playerEntity).markPropertyAsDirty(TEST_PROPERTY_WITH_OWNER_PROPAGATION);

		playerMapper.create(nearbyEntity).name = "Nearby";
		activeMapper.create(nearbyEntity).peer = nearbyPeer;

		world.process();

		Mockito.verify(ownerPeer, Mockito.times(2)).send(Mockito.any());
		Mockito.verify(nearbyPeer).send(Mockito.any());
	}
}
