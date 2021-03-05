package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Constructable;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestProgressPropertyHandler {
    private ProgressPropertyHandler progressPropertyHandler;
    private World world;
    private ComponentMapper<Constructable> constructableMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        progressPropertyHandler = new ProgressPropertyHandler(10);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(progressPropertyHandler);

        constructableMapper = world.getMapper(Constructable.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_doesNotDecorateDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.PROGRESS));
    }

    @Test
    public void givenConstructable_decoratesDataObject() {
        constructableMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals(0, dataObject.get(Properties.PROGRESS).value);
    }

    @Test
    public void givenNoProgressChange_doesNotDecorateDataObject() {
        constructableMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        syncHistory.syncedValues.put(Properties.PROGRESS, 0);

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.PROGRESS));
    }

    @Test
    public void givenSmallProgressChange_doesNotDecorateDataObject() {
        Constructable constructable = constructableMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 0 -> 5
        syncHistory.syncedValues.put(Properties.PROGRESS, 0);
        constructable.buildDuration = 100;
        constructable.buildTime = 5;

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.PROGRESS));
    }

    @Test
    public void givenLargeProgressChange_decoratesDataObject() {
        Constructable constructable = constructableMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 0 -> 80
        syncHistory.syncedValues.put(Properties.PROGRESS, 0);
        constructable.buildDuration = 100;
        constructable.buildTime = 80;

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals(80, dataObject.get(Properties.PROGRESS).value);
        Assert.assertEquals(80, syncHistory.syncedValues.get(Properties.PROGRESS));
    }

    @Test
    public void givenSmallProgressChangeToFull_decoratesDataObject() {
        Constructable constructable = constructableMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 95 -> 100
        syncHistory.syncedValues.put(Properties.PROGRESS, 95);
        constructable.buildDuration = 100;
        constructable.buildTime = 100;

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals(100, dataObject.get(Properties.PROGRESS).value);
        Assert.assertEquals(100, syncHistory.syncedValues.get(Properties.PROGRESS));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        constructableMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = progressPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.PROGRESS));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        constructableMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        progressPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.PROGRESS));
    }
}
