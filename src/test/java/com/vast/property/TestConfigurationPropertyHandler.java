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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestConfigurationPropertyHandler {
    private ConfigurationPropertyHandler configurationPropertyHandler;
    private World world;
    private ComponentMapper<Configuration> configurationMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        configurationPropertyHandler = new ConfigurationPropertyHandler(new Items(), new Recipes());

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(configurationPropertyHandler);

        configurationMapper = world.getMapper(Configuration.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenHasConfiguration_decoratesDataObject() {
        configurationMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertNotNull(dataObject.get(Properties.CONFIGURATION).value);
    }

    @Test
    public void givenNoVersionChange_doesNotDecorateDataObject() {
        Configuration configuration = configurationMapper.create(propertyEntity);

        configuration.version = 1;
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.CONFIGURATION, (short) 1)));

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.CONFIGURATION));
    }

    @Test
    public void givenVersionChange_decoratesDataObject() {
        Configuration configuration = configurationMapper.create(propertyEntity);

        // Changed from 0 -> 1
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.CONFIGURATION, (short) 0)));
        configuration.version = 1;

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals((short) 1, syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.CONFIGURATION));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        configurationMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = configurationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.CONFIGURATION));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        configurationMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        boolean decorated = configurationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.CONFIGURATION));
    }
}
