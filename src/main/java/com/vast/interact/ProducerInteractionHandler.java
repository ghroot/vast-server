package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Cost;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ProducerInteractionHandler.class);

	private ComponentMapper<Producer> producerMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	private Recipes recipes;

	public ProducerInteractionHandler(Recipes recipes) {
		super(Aspect.all(Avatar.class, Inventory.class), Aspect.all(Producer.class, Inventory.class));
		this.recipes = recipes;
	}

	@Override
	public boolean canInteract(int avatarEntity, int producerEntity) {
		return true;
	}

	@Override
	public boolean attemptStart(int avatarEntity, int producerEntity) {
		Producer producer = producerMapper.get(producerEntity);
		return producer.recipeId >= 0;
	}

	@Override
	public boolean process(int avatarEntity, int producerEntity) {
		Producer producer = producerMapper.get(producerEntity);
		Inventory producerInventory = inventoryMapper.get(producerEntity);
		Inventory playerInventory = inventoryMapper.get(avatarEntity);

		if (producer.recipeId >= 0) {
			Recipe recipe = recipes.getRecipe(producer.recipeId);

			if (producerInventory.has(recipe.getItemId())) {
				// Collect produced items
				int numberOfItemsToTransfer = producerInventory.getNumberOfItems(recipe.getItemId());
				producerInventory.remove(recipe.getItemId(), numberOfItemsToTransfer);
				playerInventory.add(recipe.getItemId(), numberOfItemsToTransfer);
				syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
				eventMapper.create(avatarEntity).addEntry("action").setData("pickedUp");
			} else {
				int numberOfItemsInRecipeCosts = 0;
				for (Cost cost : recipe.getCosts()) {
					numberOfItemsInRecipeCosts += cost.getCount();
				}
				if (producerInventory.getFreeSpace() < numberOfItemsInRecipeCosts) {
					// Not enough space in producer for recipe cost
					eventMapper.create(avatarEntity).addEntry("message").setData("It's full...").setOwnerPropagation();
				} else if (!playerInventory.has(recipe.getCosts())) {
					// Player doesn't have recipe costs in inventory
					eventMapper.create(avatarEntity).addEntry("message").setData("I don't have the required items...").setOwnerPropagation();
				} else {
					// Add items for recipe cost
					playerInventory.remove(recipe.getCosts());
					syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.INVENTORY);
					producerInventory.add(recipe.getCosts());
					syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
					eventMapper.create(avatarEntity).addEntry("action").setData("pickedUp");
				}
			}
		}

		return true;
	}

	@Override
	public void stop(int avatarEntity, int producerEntity) {
	}
}
