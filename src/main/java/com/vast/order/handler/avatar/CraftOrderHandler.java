package com.vast.order.handler.avatar;

import com.artemis.ComponentMapper;
import com.vast.component.Craft;
import com.vast.component.Event;
import com.vast.component.Inventory;
import com.vast.data.ItemRecipe;
import com.vast.data.Items;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.avatar.CraftOrderRequest;
import com.vast.order.request.OrderRequest;

public class CraftOrderHandler extends AbstractOrderHandler<CraftOrderRequest> {
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
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof CraftOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int avatarEntity) {
		return !craftMapper.has(avatarEntity);
	}

	@Override
	public void cancelOrder(int avatarEntity) {
		craftMapper.remove(avatarEntity);
	}

	@Override
	public boolean startOrder(int avatarEntity, CraftOrderRequest craftOrderRequest) {
		ItemRecipe recipe = recipes.getItemRecipe(craftOrderRequest.getRecipeId());
		Inventory inventory = inventoryMapper.get(avatarEntity);
		if (inventory.has(recipe.getCosts())) {
			craftMapper.create(avatarEntity).recipe = recipe;
			return true;
		} else {
			eventMapper.create(avatarEntity).addEntry("message").setData("I don't have the required materials...").setOwnerPropagation();
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int orderEntity, CraftOrderRequest craftOrderRequest) {
		return false;
	}
}
