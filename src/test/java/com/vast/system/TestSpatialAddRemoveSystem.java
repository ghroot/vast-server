package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Spatial;
import com.vast.component.Transform;
import com.vast.data.WorldConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class TestSpatialAddRemoveSystem {
	private World world;
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Transform> transformMapper;

	@Before
	public void setUp() {
		world = new World(new WorldConfigurationBuilder().with(
				new SpatialAddRemoveSystem(new WorldConfiguration(), new HashMap<>())
		).build());

		spatialMapper = world.getMapper(Spatial.class);
		transformMapper = world.getMapper(Transform.class);
	}

	@After
	public void tearDown() {
		spatialMapper = null;
		transformMapper = null;
		world.dispose();
		world = null;
	}

	@Test
	public void isMemberOfASpatialHashWhileAdded() {
		int entity = world.create();
		spatialMapper.create(entity);
		transformMapper.create(entity);

		world.process();

		Assert.assertNotNull(spatialMapper.get(entity).memberOfSpatialHash);

		transformMapper.remove(entity);

		world.process();

		Assert.assertNull(spatialMapper.get(entity).memberOfSpatialHash);
	}
}
