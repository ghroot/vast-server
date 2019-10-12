package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Active;
import com.vast.component.Know;
import com.vast.component.Known;
import com.vast.component.Player;
import com.vast.network.VastPeer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TestDeactivateSystem {
	@Test
	public void activatesPlayerEntity() {
		World world = new World(new WorldConfigurationBuilder().with(
			new DeactivateSystem(new HashMap<String, VastPeer>())
		).build());

		ComponentMapper<Player> playerMapper = world.getMapper(Player.class);
		ComponentMapper<Active> activeMapper = world.getMapper(Active.class);
		ComponentMapper<Know> knowMapper = world.getMapper(Know.class);
		ComponentMapper<Known> knownMapper = world.getMapper(Known.class);

		int playerEntity = world.create();
		int knownEntity = world.create();

		playerMapper.create(playerEntity).name = "TestName";
		playerMapper.create(playerEntity).id = 123;
		activeMapper.create(playerEntity);
		knowMapper.create(playerEntity).knowEntities.add(knownEntity);

		knownMapper.create(knownEntity).knownByEntities.add(playerEntity);

		world.process();

		Assert.assertFalse(activeMapper.has(playerEntity));
		Assert.assertEquals(0, playerMapper.get(playerEntity).id);
		Assert.assertFalse(knownMapper.get(knownEntity).knownByEntities.contains(playerEntity));
	}
}
