package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestRotationPropertyHandler {
    private RotationPropertyHandler rotationPropertyHandler;
    private World world;
    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        rotationPropertyHandler = new RotationPropertyHandler(3);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(rotationPropertyHandler);

        transformMapper = world.getMapper(Transform.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        interestedEntity = world.create();
        propertyEntity = world.create();
    }

    @Test
    public void givenTransform_decoratesDataObject() {
        transformMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals(0f, (float) dataObject.get(Properties.ROTATION).value, 0.001f);
    }

    @Test
    public void givenNoRotationChange_doesNotDecorateDataObject() {
        transformMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.ROTATION, 0f)));

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.ROTATION));
    }

    @Test
    public void givenSmallProgressChange_doesNotDecorateDataObject() {
        Transform transform = transformMapper.create(propertyEntity);

        // Changed from 359 -> 1
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.ROTATION, 359f)));
        transform.rotation = 1f;

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.ROTATION));
    }

    @Test
    public void givenLargeProgressChange_decoratesDataObject() {
        Transform transform = transformMapper.create(propertyEntity);

        // Changed from 3 -> 20
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.ROTATION, 3f)));
        transform.rotation = 20f;

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertEquals(20f, (float) dataObject.get(Properties.ROTATION).value, 0.001f);
        assertEquals(20f, (float) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.ROTATION), 0.001f);
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        transformMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.ROTATION));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        transformMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        rotationPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.ROTATION));
    }
}
