package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRotationPropertyHandler {
    private RotationPropertyHandler rotationPropertyHandler;
    private World world;
    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int entity;

    @Before
    public void setUp() {
        rotationPropertyHandler = new RotationPropertyHandler(3);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(rotationPropertyHandler);

        transformMapper = world.getMapper(Transform.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        entity = world.create();
    }

    @Test
    public void givenTransform_decoratesDataObject() {
        transformMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals(0f, (float) dataObject.get(Properties.ROTATION).value, 0.001f);
    }

    @Test
    public void givenNoRotationChange_doesNotDecorateDataObject() {
        transformMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        syncHistory.syncedValues.put(Properties.ROTATION, 0f);

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.ROTATION));
    }

    @Test
    public void givenSmallProgressChange_doesNotDecorateDataObject() {
        Transform transform = transformMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 359 -> 1
        syncHistory.syncedValues.put(Properties.ROTATION, 359f);
        transform.rotation = 1f;

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.ROTATION));
    }

    @Test
    public void givenLargeProgressChange_decoratesDataObject() {
        Transform transform = transformMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        // Changed from 3 -> 20
        syncHistory.syncedValues.put(Properties.ROTATION, 3f);
        transform.rotation = 20f;

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertEquals(20f, (float) dataObject.get(Properties.ROTATION).value, 0.001f);
        Assert.assertEquals(20f, (float) syncHistory.syncedValues.get(Properties.ROTATION), 0.001f);
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        transformMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.ROTATION));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        transformMapper.create(entity);
        SyncHistory syncHistory = syncHistoryMapper.create(entity);

        rotationPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.ROTATION));
    }
}
