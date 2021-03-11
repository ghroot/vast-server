package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.network.Properties;

public class CraftSystem extends IteratingSystem {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<State> stateMapper;

	private Items items;

	public CraftSystem(Items items) {
		super(Aspect.all(Craft.class, Inventory.class));
		this.items = items;
	}

	@Override
	protected void inserted(int craftEntity) {
		stateMapper.get(craftEntity).name = "crafting";
		syncMapper.create(craftEntity).markPropertyAsDirty(Properties.STATE);
	}

	@Override
	protected void removed(int craftEntity) {
		stateMapper.get(craftEntity).name = "none";
		syncMapper.create(craftEntity).markPropertyAsDirty(Properties.STATE);
	}

	@Override
	protected void process(int craftEntity) {
		Craft craft = craftMapper.get(craftEntity);

		if (craft.craftTime >= craft.recipe.getDuration()) {
			Inventory inventory = inventoryMapper.get(craftEntity);
			if (inventory.has(craft.recipe.getCosts())) {
				inventory.remove(craft.recipe.getCosts());
				inventory.add(craft.recipe.getItemId());
				syncMapper.create(craftEntity).markPropertyAsDirty(Properties.INVENTORY);
				String itemName = items.getItem(craft.recipe.getItemId()).getName();
				eventMapper.create(craftEntity).addEntry("message").setData("Crafted Item: " + itemName).setOwnerPropagation();
			}
			craftMapper.remove(craftEntity);
		} else {
			craft.craftTime += world.getDelta();
			syncMapper.create(craftEntity).markPropertyAsDirty(Properties.PROGRESS);
		}
	}
}
