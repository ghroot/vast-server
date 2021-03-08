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
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Producer.class, Inventory.class));
		this.recipes = recipes;
	}

	@Override
	public boolean canInteract(int playerEntity, int producerEntity) {
		return true;
	}

	@Override
	public boolean attemptStart(int playerEntity, int producerEntity) {
		Producer producer = producerMapper.get(producerEntity);
		Inventory producerInventory = inventoryMapper.get(producerEntity);

		if (!producer.producing) {
			return true;
		}

		if (producer.recipeId >= 0) {
			Recipe recipe = recipes.getRecipe(producer.recipeId);
			if (producerInventory.has(recipe.getItemId())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean process(int playerEntity, int producerEntity) {
		Producer producer = producerMapper.get(producerEntity);
		Inventory producerInventory = inventoryMapper.get(producerEntity);
		Inventory playerInventory = inventoryMapper.get(playerEntity);

		if (producer.recipeId >= 0) {
			Recipe recipe = recipes.getRecipe(producer.recipeId);

			if (producerInventory.has(recipe.getItemId())) {
				int numberOfItemsToTransfer = producerInventory.getNumberOfItems(recipe.getItemId());
				producerInventory.remove(recipe.getItemId(), numberOfItemsToTransfer);
				playerInventory.add(recipe.getItemId(), numberOfItemsToTransfer);
				syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
				eventMapper.create(playerEntity).addEntry("action").setData("pickedUp");
			} else {
				boolean atLeastOneItemWasTransferred = false;
				for (Cost cost : recipe.getCosts()) {
					while (playerInventory.has(cost.getItemId()) && !producerInventory.isFull()) {
						playerInventory.remove(cost.getItemId());
						producerInventory.add(cost.getItemId());
						atLeastOneItemWasTransferred = true;
					}
				}
				if (atLeastOneItemWasTransferred) {
					syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);
					syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
					eventMapper.create(playerEntity).addEntry("action").setData("pickedUp");
				}
			}
		}

		return true;
	}

	@Override
	public void stop(int playerEntity, int producerEntity) {
	}
}
