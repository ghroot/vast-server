package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Active;
import com.vast.component.Growing;
import com.vast.component.Player;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGrowingPropertyHandler {
    private GrowingPropertyHandler growingPropertyHandler;
    private World world;
    private ComponentMapper<Growing> growingMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        growingPropertyHandler = new GrowingPropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(growingPropertyHandler);

        growingMapper = world.getMapper(Growing.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_decoratesDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) dataObject.get(Properties.GROWING).value);
    }

    @Test
    public void givenGrowing_whenForced_decoratesDataObject() {
        growingMapper.create(entity).timeLeft = 1f;

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertTrue(decorated);
        Assert.assertTrue((Boolean) dataObject.get(Properties.GROWING).value);
    }

    @Test
    public void givenNotGrowing_whenForced_decoratesDataObject() {
        growingMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) dataObject.get(Properties.GROWING).value);
    }

    @Test
    public void givenNoGrowingChange_doesNotDecorateDataObject() {
        growingMapper.create(entity).timeLeft = 1f;
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        syncHistory.syncedValues.put(Properties.GROWING, true);

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.GROWING));
    }

    @Test
    public void givenGrowingChange_decoratesDataObject() {
        growingMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from growing -> not growing
        syncHistory.syncedValues.put(Properties.GROWING, true);

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) syncHistory.syncedValues.get(Properties.GROWING));
    }

    @Test
    public void givenGrowingStopped_decoratesDataObject() {
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from growing -> stopped growing
        syncHistory.syncedValues.put(Properties.GROWING, true);

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) syncHistory.syncedValues.get(Properties.GROWING));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        growingMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = growingPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
    }

    @Test
    public void givenEmptySyncHistory_whenForced_populatesSyncHistory() {
        growingMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        growingPropertyHandler.decorateDataObject(entity, new DataObject(), true);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.GROWING));
    }
}
