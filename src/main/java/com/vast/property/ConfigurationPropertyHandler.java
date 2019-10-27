package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Configuration;
import com.vast.component.SyncHistory;
import com.vast.data.*;
import com.vast.network.Properties;

import java.util.List;

public class ConfigurationPropertyHandler implements PropertyHandler {
	private static final char DATA_FIELD_DELIMITER = '|';

	private ComponentMapper<Configuration> configurationMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private Items items;
	private Buildings buildings;
	private WorldConfiguration worldConfiguration;

	public ConfigurationPropertyHandler(Items items, Buildings buildings, WorldConfiguration worldConfiguration) {
		this.items = items;
		this.buildings = buildings;
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	public byte getProperty() {
		return Properties.CONFIGURATION;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (configurationMapper.has(entity)) {
			Configuration configuration = configurationMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			short lastSyncedVersion = 0;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.CONFIGURATION)) {
				lastSyncedVersion = (short) syncHistory.syncedValues.get(Properties.CONFIGURATION);
			}
			if (force || configuration.version > lastSyncedVersion) {
				DataObject configurationData = new DataObject();

				// TODO: Cache this
				List<Item> allItems = items.getAllItems();
				String[] itemStrings = new String[allItems.size()];
				for (int i = 0; i < allItems.size(); i++) {
					Item item = allItems.get(i);
					StringBuilder itemStringBuilder = new StringBuilder();
					itemStringBuilder.append(item.getId());
					itemStringBuilder.append(DATA_FIELD_DELIMITER).append(item.getName());
					if (item instanceof CraftableItem) {
						CraftableItem craftableItem = (CraftableItem) item;
						itemStringBuilder.append(DATA_FIELD_DELIMITER).append(craftableItem.getCosts().size());
						for (Cost cost : craftableItem.getCosts()) {
							itemStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getItemId());
							itemStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getCount());
						}
					}
					itemStrings[i] = itemStringBuilder.toString();
				}
				configurationData.set((byte) 0, itemStrings);
				List<Building> allBuildings = buildings.getAllBuildings();
				String[] buildingStrings = new String[allBuildings.size()];
				for (int i = 0; i < allBuildings.size(); i++) {
					Building building = allBuildings.get(i);
					StringBuilder buildingStringBuilder = new StringBuilder();
					buildingStringBuilder.append(building.getId());
					buildingStringBuilder.append(DATA_FIELD_DELIMITER).append(building.getName());
					buildingStringBuilder.append(DATA_FIELD_DELIMITER).append(building.getCosts().size());
					for (Cost cost : building.getCosts()) {
						buildingStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getItemId());
						buildingStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getCount());
					}
					buildingStrings[i] = buildingStringBuilder.toString();
				}
				configurationData.set((byte) 1, buildingStrings);
				configurationData.set((byte) 2, (byte) worldConfiguration.cellSize);

				dataObject.set(Properties.CONFIGURATION, configurationData);
				if (syncHistory != null) {
					syncHistory.syncedValues.put(Properties.CONFIGURATION, configuration.version);
				}
				return true;
			}
		}
		return false;
	}
}
