package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Constructable;
import com.vast.component.SyncHistory;
import com.vast.data.Recipes;
import com.vast.network.Properties;
import com.vast.property.progress.ConstructableProgressPropertyHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestConstructableProgressPropertyHandler {
    private ConstructableProgressPropertyHandler constructableProgressPropertyHandler;
    private World world;
    private ComponentMapper<Constructable> constructableMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        constructableProgressPropertyHandler = new ConstructableProgressPropertyHandler(10);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(constructableProgressPropertyHandler);

        constructableMapper = world.getMapper(Constructable.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenConstructable_decoratesDataObject() {
        constructableMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals((byte) 0, dataObject.get(Properties.PROGRESS).value);
    }

    @Test
    public void givenNoProgressChange_doesNotDecorateDataObject() {
        constructableMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.PROGRESS, 0)));

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.PROGRESS));
    }

    @Test
    public void givenSmallProgressChange_doesNotDecorateDataObject() {
        Constructable constructable = constructableMapper.create(propertyEntity);

        // Changed from 0 -> 5
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.PROGRESS, 0)));
        constructable.buildDuration = 100;
        constructable.buildTime = 5;

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.PROGRESS));
    }

    @Test
    public void givenLargeProgressChange_decoratesDataObject() {
        Constructable constructable = constructableMapper.create(propertyEntity);

        // Changed from 0 -> 80
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.PROGRESS, 0)));
        constructable.buildDuration = 100;
        constructable.buildTime = 80;

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals((byte) 80, dataObject.get(Properties.PROGRESS).value);
        assertEquals(80, syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.PROGRESS));
    }

    @Test
    public void givenSmallProgressChangeToFull_decoratesDataObject() {
        Constructable constructable = constructableMapper.create(propertyEntity);

        // Changed from 95 -> 100
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.PROGRESS, 95)));
        constructable.buildDuration = 100;
        constructable.buildTime = 100;

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals((byte) 100, dataObject.get(Properties.PROGRESS).value);
        assertEquals(100, syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.PROGRESS));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        constructableMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.PROGRESS));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        constructableMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        constructableProgressPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.PROGRESS));
    }
}
