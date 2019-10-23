package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.*;
import com.vast.data.CraftableItem;
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
		stateMapper.get(craftEntity).name = null;
		syncMapper.create(craftEntity).markPropertyAsDirty(Properties.STATE);
	}

	@Override
	protected void process(int craftEntity) {
		Craft craft = craftMapper.get(craftEntity);

		craft.countdown -= world.getDelta();
		if (craft.countdown <= 0.0f) {
			Inventory inventory = inventoryMapper.get(craftEntity);
			CraftableItem itemToCraft = (CraftableItem) items.getItem(craft.itemId);
			if (inventory.has(itemToCraft.getCosts())) {
				inventory.remove(itemToCraft.getCosts());
				inventory.add(itemToCraft.getId(), 1);
				syncMapper.create(craftEntity).markPropertyAsDirty(Properties.INVENTORY);
				String capitalizedItemName = itemToCraft.getName().substring(0, 1).toUpperCase() + itemToCraft.getName().substring(1);
				eventMapper.create(craftEntity).setType("message").setData("Crafted Item: " + capitalizedItemName).setOwnerOnly(true);
			}
			craftMapper.remove(craftEntity);
		}
	}
}
