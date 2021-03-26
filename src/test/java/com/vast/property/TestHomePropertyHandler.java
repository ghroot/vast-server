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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestHomePropertyHandler {
    private HomePropertyHandler homePropertyHandler;
    private World world;
    private ComponentMapper<Home> homeMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        homePropertyHandler = new HomePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(homePropertyHandler);

        homeMapper = world.getMapper(Home.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenHasHome_decoratesDataObject() {
        homeMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new double[] {0f, 0f}, (double[]) dataObject.get(Properties.HOME).value, 0.001);
    }

    @Test
    public void givenNoHomeChange_doesNotDecorateDataObject() {
        Home home = homeMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.HOME, new Point2f(home.position))));

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.HOME));
    }

    @Test
    public void givenPositionChange_decoratesDataObject() {
        Home home = homeMapper.create(propertyEntity);

        // Moved from 0,0 -> 2,0
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.HOME, new Point2f())));
        home.position.set(2f, 0f);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new double[] {2, 0}, (double[]) dataObject.get(Properties.HOME).value, 0.001);
        assertEquals(home.position, syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.HOME));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        homeMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.HOME));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        homeMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.HOME));
    }

    @Test
    public void givenSyncHistory_syncHistoryDataIsNotSameInstanceAsPropertyData() {
        Home home = homeMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        homePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertNotSame(syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.HOME), home.position);
    }
}
