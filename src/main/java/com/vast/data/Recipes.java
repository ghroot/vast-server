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

	private Map<Integer, Recipe> recipes;

	public Recipes() {
		recipes = new HashMap<>();
	}

	public Recipes(Items items) {
		this.items = items;

		recipes = new HashMap<>();
	}

	public Recipes(String fileName, Items items) {
		this.items = items;
		try {
			recipes = new HashMap<>();
			JSONArray recipesData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream(fileName),
					Charset.defaultCharset()));
			for (Iterator<Object> it = recipesData.iterator(); it.hasNext();) {
				JSONObject recipeData = (JSONObject) it.next();
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
				Recipe recipe = null;
				if (itemName != null) {
					recipe = new Recipe(id, costs, items.getItem(itemName).getId(), duration);
				} else if (entityType != null) {
					recipe = new Recipe(id, costs, entityType);
				}
				if (recipe != null) {
					recipes.put(id, recipe);
				}
			}
		} catch (Exception exception) {
			logger.error("Error parsing recipes", exception);
		}
	}

	public List<Recipe> getAllRecipes() {
		return new ArrayList<>(recipes.values());
	}

	public List<Recipe> getItemRecipes() {
		List<Recipe> itemRecipes = new ArrayList<>();
		for (Recipe recipe : recipes.values()) {
			if (recipe.getItemId() >= 0) {
				itemRecipes.add(recipe);
			}
		}

		return itemRecipes;
	}

	public List<Recipe> getEntityRecipes() {
		List<Recipe> entityRecipes = new ArrayList<>();
		for (Recipe recipe : recipes.values()) {
			if (recipe.getEntityType() != null) {
				entityRecipes.add(recipe);
			}
		}

		return entityRecipes;
	}

	public Recipe getRecipe(int id) {
		return recipes.get(id);
	}

	public Recipe getRecipeByItemName(String itemName) {
		for (Recipe recipe : recipes.values()) {
			if (itemName.equals(items.getItem(recipe.getItemId()).getName())) {
				return recipe;
			}
		}

		return null;
	}
}
