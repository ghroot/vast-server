package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.vast.component.Spatial;
import com.vast.component.Static;
import com.vast.component.Transform;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestSpatialUpdateSystem {
	private World world;
	private Map<Integer, IntBag> spatialHashes;
	private ComponentMapper<Spatial> spatialMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Static> staticMapper;

	@Before
	public void setUp() {
		spatialHashes = new HashMap<>();

		world = new World(new WorldConfigurationBuilder().with(
				new SpatialUpdateSystem(new WorldConfiguration(), spatialHashes)
		).build());

		spatialMapper = world.getMapper(Spatial.class);
		transformMapper = world.getMapper(Transform.class);
		staticMapper = world.getMapper(Static.class);
	}

	@After
	public void tearDown() {
		spatialMapper = null;
		transformMapper = null;
		staticMapper = null;
		world.dispose();
		world = null;
	}

	private SpatialHash createSpatialHash(int entity, int x, int y) {
		SpatialHash spatialHash = new SpatialHash(x, y);

		IntBag entitiesInHash = spatialHashes.get(spatialHash.getUniqueKey());
		if (entitiesInHash == null) {
			entitiesInHash = new IntBag();
			spatialHashes.put(spatialHash.getUniqueKey(), entitiesInHash);
		}
		entitiesInHash.add(entity);

		spatialMapper.create(entity).memberOfSpatialHash = spatialHash;

		return spatialHash;
	}

	@Test
	public void updatesSpatialHashWhenMoving() {
		int entity = world.create();
		spatialMapper.create(entity);
		transformMapper.create(entity);

		SpatialHash spatialHash = createSpatialHash(entity, 0, 0);
		int spatialKeyBefore = spatialHash.getUniqueKey();

		transformMapper.get(entity).position.set(1000f, 1000f);

		world.process();

		Assert.assertFalse(spatialHashes.get(spatialKeyBefore).contains(entity));
	}
}
