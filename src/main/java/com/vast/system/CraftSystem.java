package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.Properties;
import com.vast.component.Craft;
import com.vast.component.Inventory;
import com.vast.component.Sync;
import com.vast.data.Item;
import com.vast.data.Items;

public class CraftSystem extends IteratingSystem {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Sync> syncMapper;

	private Items items;

	public CraftSystem(Items items) {
		super(Aspect.all(Craft.class, Inventory.class));
		this.items = items;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int craftEntity) {
		Craft craft = craftMapper.get(craftEntity);

		craft.countdown -= world.getDelta();
		if (craft.countdown <= 0.0f) {
			Inventory inventory = inventoryMapper.get(craftEntity);
			Item itemToCraft = items.getItem(craft.itemType);
			if (inventory.has(itemToCraft.getCosts())) {
				inventory.remove(itemToCraft.getCosts());
				inventory.add(craft.itemType, 1);
				syncMapper.create(craftEntity).markPropertyAsDirty(Properties.INVENTORY);
			}
			craftMapper.remove(craftEntity);
		}
	}
}
