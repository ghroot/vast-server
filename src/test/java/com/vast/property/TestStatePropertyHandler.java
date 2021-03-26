package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.State;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestStatePropertyHandler {
    private StatePropertyHandler statePropertyHandler;
    private World world;
    private ComponentMapper<State> stateMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        statePropertyHandler = new StatePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(statePropertyHandler);

        stateMapper = world.getMapper(State.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenHasState_decoratesDataObject() {
        stateMapper.create(propertyEntity).name = "testState";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals("testState", dataObject.get(Properties.STATE).value);
    }

    @Test
    public void givenNoStateChange_doesNotDecorateDataObject() {
        stateMapper.create(propertyEntity).name = "testState";

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.STATE, "testState")));

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.STATE));
    }

    @Test
    public void givenStateChange_decoratesDataObject() {
        State state = stateMapper.create(propertyEntity);

        // Moved from "testState1" -> "testState2"
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.STATE, "testState1")));
        state.name = "testState2";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals("testState2", dataObject.get(Properties.STATE).value);
        assertEquals("testState2", syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.STATE));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        stateMapper.create(propertyEntity).name = "testState";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals("testState", dataObject.get(Properties.STATE).value);
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        stateMapper.create(propertyEntity).name = "testState";

        syncHistoryMapper.create(interestedEntity);

        statePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.STATE));
    }
}
