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

public class TestStatePropertyHandler {
    private StatePropertyHandler statePropertyHandler;
    private World world;
    private ComponentMapper<State> stateMapper;
    private int entity;

    @Before
    public void setUp() {
        statePropertyHandler = new StatePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(statePropertyHandler);

        stateMapper = world.getMapper(State.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_doesNotDecorateDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.STATE));
    }

    @Test
    public void givenHasState_decoratesDataObject() {
        stateMapper.create(entity).name = "testState";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals("testState", dataObject.get(Properties.STATE).value);
    }

    @Test
    public void givenNoStateChange_doesNotDecorateDataObject() {
        stateMapper.create(entity).name = "testState";
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        syncHistory.syncedValues.put(Properties.STATE, "testState");

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.STATE));
    }

    @Test
    public void givenStateChange_decoratesDataObject() {
        State state = stateMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Moved from "testState1" -> "testState2"
        syncHistory.syncedValues.put(Properties.STATE, "testState1");
        state.name = "testState2";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals("testState2", dataObject.get(Properties.STATE).value);
        Assert.assertEquals("testState2", syncHistory.syncedValues.get(Properties.STATE));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        stateMapper.create(entity).name = "testState";

        DataObject dataObject = new DataObject();
        boolean decorated = statePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals("testState", dataObject.get(Properties.STATE).value);
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        stateMapper.create(entity).name = "testState";
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        statePropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.STATE));
    }
}
