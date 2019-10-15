package com.vast.system;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.vast.component.Scan;
import com.vast.component.Spatial;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestScanSystem {
	@Test
	public void scanFindsNearbyEntityIncludingSelf() {
		WorldConfiguration worldConfiguration = new WorldConfiguration();
		Map<Integer, Set<Integer>> spatialHashes = new HashMap<>();
		World world = new World(new WorldConfigurationBuilder().with(
				new ScanSystem(worldConfiguration, spatialHashes)
		).build());

		ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
		ComponentMapper<Spatial> spatialMapper = world.getMapper(Spatial.class);

		int playerEntity = world.create();
		scanMapper.create(playerEntity);
		SpatialHash playerSpatialHash = new SpatialHash(0, 0);
		spatialMapper.create(playerEntity).memberOfSpatialHash = playerSpatialHash;

		int otherEntity = world.create();
        SpatialHash otherSpatialHash = new SpatialHash(0, 0);
		spatialMapper.create(otherEntity).memberOfSpatialHash = otherSpatialHash;

        spatialHashes.put(playerSpatialHash.getUniqueKey(), new HashSet<Integer>(Arrays.asList(playerEntity, otherEntity)));

		world.process();

		Assert.assertEquals(2, scanMapper.get(playerEntity).nearbyEntities.size());
		Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(playerEntity));
        Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(otherEntity));
	}

    @Test
    public void scanDoesNotFindsFarEntityOnlySelf() {
        WorldConfiguration worldConfiguration = new WorldConfiguration();
        Map<Integer, Set<Integer>> spatialHashes = new HashMap<>();
        World world = new World(new WorldConfigurationBuilder().with(
                new ScanSystem(worldConfiguration, spatialHashes)
        ).build());

        ComponentMapper<Scan> scanMapper = world.getMapper(Scan.class);
        ComponentMapper<Spatial> spatialMapper = world.getMapper(Spatial.class);

        int playerEntity = world.create();
        scanMapper.create(playerEntity).distance = 2f;
        SpatialHash playerSpatialHash = new SpatialHash(0, 0);
        spatialMapper.create(playerEntity).memberOfSpatialHash = playerSpatialHash;

        int otherEntity = world.create();
        SpatialHash otherSpatialHash = new SpatialHash(100, 0);
        spatialMapper.create(otherEntity).memberOfSpatialHash = otherSpatialHash;

        spatialHashes.put(playerSpatialHash.getUniqueKey(), new HashSet<Integer>(Arrays.asList(playerEntity)));
        spatialHashes.put(otherSpatialHash.getUniqueKey(), new HashSet<Integer>(Arrays.asList(otherEntity)));

        world.process();

        Assert.assertEquals(1, scanMapper.get(playerEntity).nearbyEntities.size());
        Assert.assertTrue(scanMapper.get(playerEntity).nearbyEntities.contains(playerEntity));
    }
}
