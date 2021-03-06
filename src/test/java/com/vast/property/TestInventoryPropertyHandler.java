package com.vast.property;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Inventory;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestInventoryPropertyHandler {
    private InventoryPropertyHandler inventoryPropertyHandler;
    private World world;
    private ComponentMapper<Inventory> inventoryMapper;
    private int entity;

    @Before
    public void setUp() {
        inventoryPropertyHandler = new InventoryPropertyHandler();

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(inventoryPropertyHandler);

        inventoryMapper = world.getMapper(Inventory.class);

        entity = world.create();
    }

    @Test
    public void givenEmptyEntity_doesNotDecorateDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.INVENTORY));
    }

    @Test
    public void givenHasInventory_decoratesDataObject() {
        inventoryMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new short[0], (short[]) dataObject.get(Properties.INVENTORY).value);
    }

    @Test
    public void givenNoInventoryChange_doesNotDecorateDataObject() {
        Inventory inventory = inventoryMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        syncHistory.syncedValues.put(Properties.INVENTORY, new short[0]);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.INVENTORY));
    }

    @Test
    public void givenInventoryChange_decoratesDataObject() {
        Inventory inventory = inventoryMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Changed from [] -> [1]
        syncHistory.syncedValues.put(Properties.INVENTORY, new short[0]);
        inventory.add(0);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new short[] {1}, (short[]) dataObject.get(Properties.INVENTORY).value);
        Assert.assertArrayEquals(inventory.items, (short[]) syncHistory.syncedValues.get(Properties.INVENTORY));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        inventoryMapper.create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = inventoryPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertTrue(dataObject.contains(Properties.INVENTORY));
    }

    @Test
    public void givenEmptySyncHistory_populatesSyncHistory() {
        inventoryMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        inventoryPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertTrue(syncHistory.syncedValues.containsKey(Properties.INVENTORY));
    }

    @Test
    public void givenSyncHistory_syncHistoryDataIsNotSameInstanceAsPropertyData() {
        Inventory inventory = inventoryMapper.create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        inventoryPropertyHandler.decorateDataObject(entity, new DataObject(), false);

        Assert.assertNotSame(syncHistory.syncedValues.get(Properties.INVENTORY), inventory.items);
    }
}
