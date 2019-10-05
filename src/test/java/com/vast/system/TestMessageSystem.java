package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Active;
import com.vast.component.Message;
import com.vast.component.Player;
import com.vast.network.VastPeer;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class TestMessageSystem {
	@Test
	public void sendsMessage() {
		Map<String, VastPeer> peers = new HashMap<>();
		VastPeer peer = Mockito.mock(VastPeer.class);
		Mockito.when(peer.getId()).thenReturn(123L);
		peers.put("TestName", peer);
		World world = new World(new WorldConfigurationBuilder().with(
			new MessageSystem(peers)
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Message> messageMapper = world.getMapper(Message.class);
		int entityId = world.create();
		playerMapper.create(entityId).name = "TestName";
		activeMapper.create(entityId);
		messageMapper.create(entityId).text = "TestText";

		world.process();

		Mockito.verify(peer).send(Mockito.any());
	}
}
