package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.ItemRecipe;
import com.vast.data.Items;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.Properties;

public class ProducerSystem extends IteratingSystem {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Producer> producerMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<State> stateMapper;

	private Items items;
	private Recipes recipes;

	public ProducerSystem(Items items, Recipes recipes) {
		super(Aspect.all(Producer.class, Inventory.class));
		this.items = items;
		this.recipes = recipes;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int producerEntity) {
		Producer producer = producerMapper.get(producerEntity);
		Inventory inventory = inventoryMapper.get(producerEntity);

		ItemRecipe recipe = recipes.getItemRecipe(producer.recipeId);

		if (producer.producing) {
			if (producer.time >= recipe.getDuration()) {
				inventory.add(recipe.getItemId());
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
				producer.time = 0f;
				producer.producing = false;
				stateMapper.get(producerEntity).name = "none";
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.STATE);
			} else {
				producer.time += world.delta;
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.PROGRESS);
			}
		} else if (producer.recipeId >= 0) {
			if (inventory.has(recipe.getCosts())) {
				inventory.remove(recipe.getCosts());
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.INVENTORY);
				producer.producing = true;
				stateMapper.get(producerEntity).name = "producing";
				syncMapper.create(producerEntity).markPropertyAsDirty(Properties.STATE);
			}
		}
	}
}
