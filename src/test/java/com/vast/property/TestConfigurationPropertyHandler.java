package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Configuration;
import com.vast.component.SyncHistory;
import com.vast.data.Items;
import com.vast.data.Recipes;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestConfigurationPropertyHandler {
    private ConfigurationPropertyHandler configurationPropertyHandler;
    private World world;
    private ComponentMapper<Configuration> configurationMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        configurationPropertyHandler = new ConfigurationPropertyHandler(new Items(), new Recipes());

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(configurationPropertyHandler);

        configurationMapper = world.getMapper(Configuration.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenHasConfiguration_decoratesDataObject() {
        configurationMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertNotNull(dataObject.get(Properties.CONFIGURATION).value);
    }

    @Test
    public void givenNoVersionChange_doesNotDecorateDataObject() {
        Configuration configuration = configurationMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        configuration.version = 1;
        syncHistory.syncedValues.put(Properties.CONFIGURATION, (short) 1);

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.CONFIGURATION));
    }

    @Test
    public void givenVersionChange_decoratesDataObject() {
        Configuration configuration = configurationMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 0 -> 1
        syncHistory.syncedValues.put(Properties.CONFIGURATION, (short) 0);
        configuration.version = 1;

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals((short) 1, syncHistory.syncedValues.get(Properties.CONFIGURATION));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        configurationMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.CONFIGURATION));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        configurationMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        configurationPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.CONFIGURATION));
    }
}
