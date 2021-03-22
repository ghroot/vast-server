package com.vast.order;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Craft;
import com.vast.component.Event;
import com.vast.component.Inventory;
import com.vast.data.Items;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.MessageCodes;

public class CraftOrderHandler implements OrderHandler {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Event> eventMapper;

	private Recipes recipes;
	private Items items;

	public CraftOrderHandler(Recipes recipes, Items items) {
		this.recipes = recipes;
		this.items = items;
	}

	@Override

	public void initialize() {
	}

	@Override
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.CRAFT;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !craftMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		craftMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, short messageCode, DataObject dataObject) {
		int recipeId = (byte) dataObject.get(MessageCodes.CRAFT_RECIPE_ID).value;
		Recipe recipe = recipes.getRecipe(recipeId);
		Inventory inventory = inventoryMapper.get(orderEntity);
		if (inventory.has(recipe.getCosts())) {
			craftMapper.create(orderEntity).recipe = recipe;
			return true;
		} else {
			eventMapper.create(orderEntity).addEntry("message").setData("I don't have the required materials...").setOwnerPropagation();
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		return false;
	}
}
