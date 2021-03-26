package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Fueled;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFueledPropertyHandler {
    private FueledPropertyHandler fueledPropertyHandler;
    private World world;
    private ComponentMapper<Fueled> fueledMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        fueledPropertyHandler = new FueledPropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(fueledPropertyHandler);

        fueledMapper = world.getMapper(Fueled.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenHasFueled_decoratesDataObject() {
        fueledMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenNoFueledChange_doesNotDecorateDataObject() {
        fueledMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        syncHistory.syncedValues.put(Properties.FUELED, false);

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertFalse(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenFueledChange_decoratesDataObject() {
        Fueled fueled = fueledMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from not fueled -> fueled
        syncHistory.syncedValues.put(Properties.FUELED, false);
        fueled.timeLeft = 1f;

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue((Boolean) syncHistory.syncedValues.get(Properties.FUELED));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        fueledMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        fueledMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        fueledPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.FUELED));
    }
}
