package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Avatar;
import com.vast.component.Observed;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestActivePropertyHandler {
    private ActivePropertyHandler activePropertyHandler;
    private World world;
    private ComponentMapper<Avatar> avatarMapper;
    private ComponentMapper<Observed> observedMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        activePropertyHandler = new ActivePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(activePropertyHandler);

        avatarMapper = world.getMapper(Avatar.class);
        observedMapper = world.getMapper(Observed.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_doesNotDecorateDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.ACTIVE));
    }

    @Test
    public void givenObservedAvatar_decoratesDataObject() {
        avatarMapper.create(entity);
        observedMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue((Boolean) dataObject.get(Properties.ACTIVE).value);
    }

    @Test
    public void givenNoActiveChange_doesNotDecorateDataObject() {
        avatarMapper.create(entity);
        observedMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        syncHistory.syncedValues.put(Properties.ACTIVE, true);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.POSITION));
    }

    @Test
    public void givenActiveChange_decoratesDataObject() {
        avatarMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from active -> inactive
        syncHistory.syncedValues.put(Properties.ACTIVE, true);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) syncHistory.syncedValues.get(Properties.ACTIVE));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        avatarMapper.create(entity);
        observedMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.ACTIVE));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        avatarMapper.create(entity);
        observedMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        activePropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.ACTIVE));
    }
}
