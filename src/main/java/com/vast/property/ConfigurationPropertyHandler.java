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

	private DataObject configurationData;

	public ConfigurationPropertyHandler(Items items, Recipes recipes) {
		createConfigurationData(items, recipes);
	}

	private void createConfigurationData(Items items, Recipes recipes) {
		configurationData = new DataObject();

		List<Item> allItems = items.getAllItems();
		String[] itemStrings = new String[allItems.size()];
		for (int i = 0; i < allItems.size(); i++) {
			Item item = allItems.get(i);
			StringBuilder itemStringBuilder = new StringBuilder();
			itemStringBuilder.append(item.getId());
			itemStringBuilder.append(DATA_FIELD_DELIMITER).append(item.getName());
			itemStrings[i] = itemStringBuilder.toString();
		}
		configurationData.set((byte) 0, itemStrings);

		List<Recipe> itemsRecipes = recipes.getItemRecipes();
		String[] itemRecipeStrings = new String[itemsRecipes.size()];
		for (int i = 0; i < itemsRecipes.size(); i++) {
			Recipe itemRecipe = itemsRecipes.get(i);
			StringBuilder recipeStringBuilder = new StringBuilder();
			recipeStringBuilder.append(itemRecipe.getId());
			recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(itemRecipe.getItemId());
			recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(itemRecipe.getDuration());
			recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(itemRecipe.getCosts().size());
			for (Cost cost : itemRecipe.getCosts()) {
				recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getItemId());
				recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getCount());
			}
			itemRecipeStrings[i] = recipeStringBuilder.toString();
		}
		configurationData.set((byte) 1, itemRecipeStrings);

		List<Recipe> entityRecipes = recipes.getEntityRecipes();
		String[] entityRecipeStrings = new String[entityRecipes.size()];
		for (int i = 0; i < entityRecipes.size(); i++) {
			Recipe entityRecipe = entityRecipes.get(i);
			StringBuilder recipeStringBuilder = new StringBuilder();
			recipeStringBuilder.append(entityRecipe.getId());
			recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(entityRecipe.getEntityType());
			recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(entityRecipe.getCosts().size());
			for (Cost cost : entityRecipe.getCosts()) {
				recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getItemId());
				recipeStringBuilder.append(DATA_FIELD_DELIMITER).append(cost.getCount());
			}
			entityRecipeStrings[i] = recipeStringBuilder.toString();
		}
		configurationData.set((byte) 2, entityRecipeStrings);
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
