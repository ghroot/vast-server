package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Sync;
import com.vast.data.Properties;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class TestActivateSystem {
	@Test
	public void activatesPlayerEntity() {
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		peers.put("TestName", peer);
		World world = new World(new WorldConfigurationBuilder().with(
			new ActivateSystem(peers)
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		int entityId = world.create();
		playerMapper.create(entityId).name = "TestName";

		world.process();

		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Sync> syncMapper = world.getMapper(Sync.class);
		Assert.assertTrue(activeMapper.has(entityId));
		Assert.assertTrue(syncMapper.get(entityId).isPropertyDirty(Properties.ACTIVE));
		Assert.assertEquals(123L, playerMapper.get(entityId).id);
	}
}
