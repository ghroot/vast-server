package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Home;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.vecmath.Point2f;

public class TestHomePropertyHandler {
    private HomePropertyHandler homePropertyHandler;
    private World world;
    private ComponentMapper<Home> homeMapper;
    private int entity;

    @Before
    public void setUp() {
        homePropertyHandler = new HomePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(homePropertyHandler);

        homeMapper = world.getMapper(Home.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_doesNotDecorateDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.HOME));
    }

    @Test
    public void givenHasHome_decoratesDataObject() {
        homeMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new double[] {0f, 0f}, (double[]) dataObject.get(Properties.HOME).value, 0.001);
    }

    @Test
    public void givenNoHomeChange_doesNotDecorateDataObject() {
        Home home = homeMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        syncHistory.syncedValues.put(Properties.HOME, new Point2f(home.position));

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.HOME));
    }

    @Test
    public void givenPositionChange_decoratesDataObject() {
        Home home = homeMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Moved from 0,0 -> 2,0
        syncHistory.syncedValues.put(Properties.HOME, new Point2f());
        home.position.set(2f, 0f);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new double[] {2, 0}, (double[]) dataObject.get(Properties.HOME).value, 0.001);
        Assert.assertEquals(home.position, syncHistory.syncedValues.get(Properties.HOME));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        homeMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.HOME));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        homeMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        homePropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.HOME));
    }

    @Test
    public void givenSyncHistory_syncHistoryDataIsNotSameInstanceAsPropertyData() {
        Home home = homeMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        homePropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertNotSame(syncHistory.syncedValues.get(Properties.HOME), home.position);
    }
}
