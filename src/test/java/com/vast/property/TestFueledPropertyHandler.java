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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestFueledPropertyHandler {
    private FueledPropertyHandler fueledPropertyHandler;
    private World world;
    private ComponentMapper<Fueled> fueledMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        fueledPropertyHandler = new FueledPropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(fueledPropertyHandler);

        fueledMapper = world.getMapper(Fueled.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenHasFueled_decoratesDataObject() {
        fueledMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenNoFueledChange_doesNotDecorateDataObject() {
        fueledMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.FUELED, false)));

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertFalse(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenFueledChange_decoratesDataObject() {
        Fueled fueled = fueledMapper.create(propertyEntity);

        // Changed from not fueled -> fueled
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.FUELED, false)));
        fueled.timeLeft = 1f;

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue((Boolean) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.FUELED));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        fueledMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = fueledPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.FUELED));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        fueledMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        fueledPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.FUELED));
    }
}
