package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Inventory;
import com.vast.component.SyncHistory;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestInventoryPropertyHandler {
    private InventoryPropertyHandler inventoryPropertyHandler;
    private World world;
    private ComponentMapper<Inventory> inventoryMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;
    private int interestedEntity;
    private int propertyEntity;

    @Before
    public void setUp() {
        inventoryPropertyHandler = new InventoryPropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(inventoryPropertyHandler);

        inventoryMapper = world.getMapper(Inventory.class);
        syncHistoryMapper = world.getMapper(SyncHistory.class);

        propertyEntity = world.create();
    }

    @Test
    public void givenHasInventory_decoratesDataObject() {
        inventoryMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new short[0], (short[]) dataObject.get(Properties.INVENTORY).value);
    }

    @Test
    public void givenNoInventoryChange_doesNotDecorateDataObject() {
        Inventory inventory = inventoryMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.INVENTORY, new short[0])));

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertFalse(decorated);
        assertNull(dataObject.get(Properties.INVENTORY));
    }

    @Test
    public void givenInventoryChange_decoratesDataObject() {
        Inventory inventory = inventoryMapper.create(propertyEntity);

        // Changed from [] -> [1]
        syncHistoryMapper.create(interestedEntity).syncedValues.put(propertyEntity, new HashMap<>(Map.of(Properties.INVENTORY, new short[0])));
        inventory.add(0);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertArrayEquals(new short[] {1}, (short[]) dataObject.get(Properties.INVENTORY).value);
        assertArrayEquals(inventory.items, (short[]) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.INVENTORY));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        inventoryMapper.create(propertyEntity);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, dataObject, false);

        assertTrue(decorated);
        assertTrue(dataObject.contains(Properties.INVENTORY));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        inventoryMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertTrue(syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, Properties.INVENTORY));
    }

    @Test
    public void givenSyncHistory_syncHistoryDataIsNotSameInstanceAsPropertyData() {
        Inventory inventory = inventoryMapper.create(propertyEntity);

        syncHistoryMapper.create(interestedEntity);

        inventoryPropertyHandler.decorateDataObject(interestedEntity, propertyEntity, new DataObject(), false);

        assertNotSame(syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, Properties.INVENTORY), inventory.items);
    }
}
