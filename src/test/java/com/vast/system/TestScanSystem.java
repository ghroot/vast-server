package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.artemis.utils.IntBag;
import com.vast.component.Scan;
import com.vast.component.Spatial;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TestScanSystem {
	private World world;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Spatial> spatialMapper;
	private Map<Integer, IntBag> spatialHashes;

	@Before
	public void setUp() {
		spatialHashes = new HashMap<>();

		world = new World(new WorldConfigurationBuilder().with(
				new ScanSystem(new WorldConfiguration(), spatialHashes)
		).build());

		scanMapper = world.getMapper(Scan.class);
		spatialMapper = world.getMapper(Spatial.class);
	}

	@After
	public void tearDown() {
		scanMapper = null;
		spatialMapper = null;
		world.dispose();
		world = null;
		spatialHashes = null;
	}

	private void createSpatialHash(int entity, int x, int y) {
		SpatialHash spatialHash = new SpatialHash(x, y);

		IntBag entitiesInHash = spatialHashes.get(spatialHash.getUniqueKey());
		if (entitiesInHash == null) {
			entitiesInHash = new IntBag();
			spatialHashes.put(spatialHash.getUniqueKey(), entitiesInHash);
		}
		entitiesInHash.add(entity);

		spatialMapper.create(entity).memberOfSpatialHash = spatialHash;
	}

	@Test
	public void scanFindsNearbyEntityIncludingSelf() {
		int playerEntity = world.create();
		scanMapper.create(playerEntity);
		createSpatialHash(playerEntity, 0, 0);

		int otherEntity = world.create();
		createSpatialHash(otherEntity, 0, 0);

		world.process();

		Assert.assertEquals(2, scanMapper.get(playerEntity).nearbyEntities.size());
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(playerEntity));
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(otherEntity));
	}

	@Test
	public void scanDoesNotFindsFarEntityOnlySelf() {
		int playerEntity = world.create();
		scanMapper.create(playerEntity);
		createSpatialHash(playerEntity, 0, 0);

		int otherEntity = world.create();
		createSpatialHash(otherEntity, 100, 0);

		world.process();

		Assert.assertEquals(1, scanMapper.get(playerEntity).nearbyEntities.size());
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(playerEntity));
	}

	@Test
	public void takesScanDistanceIntoAccount() {
		int playerEntity = world.create();
		scanMapper.create(playerEntity).distance = 1000f;
		createSpatialHash(playerEntity, 0, 0);

		int otherEntity = world.create();
		createSpatialHash(otherEntity, 100, 0);

		world.process();

		Assert.assertEquals(2, scanMapper.get(playerEntity).nearbyEntities.size());
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(playerEntity));
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(otherEntity));
	}
}
