package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

public class Recipes {
	private static final Logger logger = LoggerFactory.getLogger(Recipes.class);

	private Items items;

	private List<Recipe> allRecipes;
	private List<ItemRecipe> itemRecipes;
	private List<EntityRecipe> entityRecipes;
	private Map<Integer, Recipe> recipesById;

	public Recipes() {
		allRecipes = new ArrayList<>();
		itemRecipes = new ArrayList<>();
		entityRecipes = new ArrayList<>();
		recipesById = new HashMap<>();
	}

	public Recipes(Items items) {
		this.items = items;

		allRecipes = new ArrayList<>();
		itemRecipes = new ArrayList<>();
		entityRecipes = new ArrayList<>();
		recipesById = new HashMap<>();
	}

	public Recipes(String fileName, Items items) {
		this.items = items;
		try {
			allRecipes = new ArrayList<>();
			itemRecipes = new ArrayList<>();
			entityRecipes = new ArrayList<>();
			recipesById = new HashMap<>();
			JSONArray recipesData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream(fileName),
					Charset.defaultCharset()));
			for (int i = 0; i < recipesData.length(); i++) {
				JSONObject recipeData = (JSONObject) recipesData.get(i);
				int id = -1;
				String itemName = null;
				float duration = 0;
				String entityType = null;
				Set<Cost> costs = new HashSet<>();
				for (String key : recipeData.keySet()) {
					Object value = recipeData.get(key);
					switch (key) {
						case "id":
							id = (int) value;
							break;
						case "itemName":
							itemName = (String) value;
							break;
						case "duration":
							duration = Float.parseFloat(String.valueOf(value));
							break;
						case "entityType":
							entityType = (String) value;
							break;
						case "cost":
							JSONObject costData = (JSONObject) value;
							for (String costItemName : costData.keySet()) {
								int amount = costData.getInt(costItemName);
								costs.add(new Cost(items.getItem(costItemName).getId(), amount));
							}
							break;
					}
				}
				if (itemName != null) {
					ItemRecipe itemRecipe = new ItemRecipe(id, costs, items.getItem(itemName).getId(), duration);
					allRecipes.add(itemRecipe);
					itemRecipes.add(itemRecipe);
					recipesById.put(id, itemRecipe);
				} else if (entityType != null) {
					EntityRecipe entityRecipe = new EntityRecipe(id, costs, entityType);
					allRecipes.add(entityRecipe);
					entityRecipes.add(entityRecipe);
					recipesById.put(id, entityRecipe);
				}
			}
		} catch (Exception exception) {
			logger.error("Error parsing recipes", exception);
		}
	}

	public List<Recipe> getAllRecipes() {
		return allRecipes;
	}

	public List<ItemRecipe> getItemRecipes() {
		return itemRecipes;
	}

	public ItemRecipe getItemRecipe(int id) {
		return (ItemRecipe) recipesById.get(id);
	}

	public List<EntityRecipe> getEntityRecipes() {
		return entityRecipes;
	}

	public EntityRecipe getEntityRecipe(int id) {
		return (EntityRecipe) recipesById.get(id);
	}

	public Recipe getRecipeByItemName(String itemName) {
		for (ItemRecipe recipe : itemRecipes) {
			if (itemName.equals(items.getItem(recipe.getItemId()).getName())) {
				return recipe;
			}
		}

		return null;
	}
}
