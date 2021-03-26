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

import javax.vecmath.Point2f;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestPositionPropertyHandler {
    private PositionPropertyHandler positionPropertyHandler;
    private World world;
    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        positionPropertyHandler = new PositionPropertyHandler(1f);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(positionPropertyHandler);

        transformMapper = world.getMapper(Transform.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        propertyEntity = world.create();
    }

    @Test
    public void givenHasTransform_decoratesDataObject() {
        transformMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new double[] {0f, 0f}, (double[]) dataObject.get(Properties.POSITION).value, 0.001);
    }

    @Test
    public void givenNoPositionChange_doesNotDecorateDataObject() {
        Transform transform = transformMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.POSITION, new Point2f(transform.position))));

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.POSITION));
    }

    @Test
    public void givenPositionChange_decoratesDataObject() {
        Transform transform = transformMapper.create(propertyEntity);

        // Moved from 0,0 -> 2,0
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.POSITION, new Point2f())));
        transform.position.set(2f, 0f);

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new double[] {2, 0}, (double[]) dataObject.get(Properties.POSITION).value, 0.001);
        assertEquals(transform.position, syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.POSITION));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        transformMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.POSITION));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        transformMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.POSITION));
    }

    @Test
    public void givenSyncHistory_syncHistoryDataIsNotSameInstanceAsPropertyData() {
        Transform transform = transformMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        positionPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertNotSame(syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.POSITION), transform.position);
    }
}
