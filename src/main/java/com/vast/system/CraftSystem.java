package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.data.Item;
import com.vast.data.Items;

public class CraftSystem extends IteratingSystem {
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message>  messageMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Event> eventMapper;

	private Items items;

	public CraftSystem(Items items) {
		super(Aspect.all(Craft.class, Inventory.class));
		this.items = items;
	}

	@Override
	protected void inserted(int craftEntity) {
		if (playerMapper.has(craftEntity) && activeMapper.has(craftEntity)) {
			eventMapper.create(craftEntity).name = "startedCrafting";
		}
	}

	@Override
	protected void removed(int craftEntity) {
		if (playerMapper.has(craftEntity) && activeMapper.has(craftEntity)) {
			eventMapper.create(craftEntity).name = "stoppedCrafting";
		}
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
				inventory.add(itemToCraft.getType(), 1);
				syncMapper.create(craftEntity).markPropertyAsDirty(Properties.INVENTORY);
				String capitalizedItemName = itemToCraft.getName().substring(0, 1).toUpperCase() + itemToCraft.getName().substring(1);
				messageMapper.create(craftEntity).text = "Crafted Item: " + capitalizedItemName;
			}
			craftMapper.remove(craftEntity);
		}
	}
}
