package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.*;
import com.vast.interact.InteractionHandler;
import com.vast.order.request.avatar.*;

import javax.vecmath.Point2f;
import java.util.List;
import java.util.Random;

public class HumanBehaviour extends AbstractBehaviour {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<OrderQueue> orderQueueMapper;
	private ComponentMapper<Order> orderMapper;

	private Items items;
	private Recipes recipes;

	public HumanBehaviour(InteractionHandler[] interactionHandlers, WorldConfiguration worldConfiguration,
						  Random random, Items items, Recipes recipes) {
		super(interactionHandlers, worldConfiguration, random);
		this.items = items;
		this.recipes = recipes;
	}

	@Override
	public void process(int avatarEntity) {
		if (orderQueueMapper.has(avatarEntity) || orderMapper.has(avatarEntity)) {
			return;
		}

		float roll = random.nextFloat() * 100f;
		if (roll <= 0.2f) {
			orderQueueMapper.create(avatarEntity).requests.add(new SetHomeOrderRequest());
		} else if (roll <= 2f) {
			Recipe recipe;
			if (random.nextFloat() <= 0.5f) {
				recipe = recipes.getRecipeByItemName("Axe");
			} else {
				recipe = recipes.getRecipeByItemName("Pickaxe");
			}
			Inventory inventory = inventoryMapper.get(avatarEntity);
			if (inventory.has(recipe.getCosts())) {
				orderQueueMapper.create(avatarEntity).requests.add(new CraftOrderRequest(recipe.getId()));
			}
		} else if (roll <= 5f) {
			orderQueueMapper.create(avatarEntity).requests.add(new EmoteOrderRequest(0));
		} else if (roll <= 10f) {
			orderQueueMapper.create(avatarEntity).requests.add(new PlantOrderRequest());
		} else if (roll <= 45f) {
			List<Integer> nearbyEntities = getNearbyEntities(avatarEntity);
			if (nearbyEntities.size() > 0) {
				int randomIndex = (int) (random.nextFloat() * nearbyEntities.size());
				int randomNearbyEntity = nearbyEntities.get(randomIndex);
				Inventory inventory = inventoryMapper.get(avatarEntity);
				boolean skip = false;
				if (typeMapper.has(randomNearbyEntity)) {
					if (typeMapper.get(randomNearbyEntity).type.equals("tree") && !hasItemWithTag(inventory, "axe")) {
						skip = true;
					} else if (typeMapper.get(randomNearbyEntity).type.equals("rock") && !hasItemWithTag(inventory, "pickaxe")) {
						skip = true;
					}
				}
				if (!skip && canInteract(avatarEntity, randomNearbyEntity)) {
					orderQueueMapper.create(avatarEntity).requests.add(new InteractOrderRequest(randomNearbyEntity));
				}
			}
		} else {
			Point2f randomMovePosition = getRandomMovePosition(transformMapper.get(avatarEntity).position, 2f);
			if (randomMovePosition != null) {
				orderQueueMapper.create(avatarEntity).requests.add(new MoveOrderRequest(randomMovePosition));
			}
		}
	}

	private boolean hasItemWithTag(Inventory inventory, String tag) {
		for (int itemId = 0; itemId < inventory.items.length; itemId++) {
			if (inventory.items[itemId] > 0) {
				Item item = items.getItem(itemId);
				if (item.hasTag(tag)) {
					return true;
				}
			}
		}
		return false;
	}
}
