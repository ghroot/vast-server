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

import java.util.HashMap;
import java.util.Map;

public class TestActivePropertyHandler {
    private ActivePropertyHandler activePropertyHandler;
    private World world;
    private ComponentMapper<Avatar> avatarMapper;
    private ComponentMapper<Observed> observedMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        activePropertyHandler = new ActivePropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(activePropertyHandler);

        avatarMapper = world.getMapper(Avatar.class);
        observedMapper = world.getMapper(Observed.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenObservedAvatar_decoratesDataObject() {
        avatarMapper.create(propertyEntity);
        observedMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue((Boolean) dataObject.get(Properties.ACTIVE).value);
    }

    @Test
    public void givenNoActiveChange_doesNotDecorateDataObject() {
        avatarMapper.create(propertyEntity);
        observedMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.ACTIVE, true)));

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.POSITION));
    }

    @Test
    public void givenActiveChange_decoratesDataObject() {
        avatarMapper.create(propertyEntity);

        // Changed from active -> inactive
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.ACTIVE, true)));

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertFalse((Boolean) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.ACTIVE));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        avatarMapper.create(propertyEntity);
        observedMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = activePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.ACTIVE));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        avatarMapper.create(propertyEntity);
        observedMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        activePropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        Assert.assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.ACTIVE));
    }
}
