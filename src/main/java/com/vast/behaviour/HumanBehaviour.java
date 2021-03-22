package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.component.*;
import com.vast.data.*;
import com.vast.interact.InteractionHandler;
import com.vast.network.IncomingRequest;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HumanBehaviour extends AbstractBehaviour {
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Type> typeMapper;

	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;
	private Items items;
	private Recipes recipes;

	public HumanBehaviour(InteractionHandler[] interactionHandlers, WorldConfiguration worldConfiguration,
						  Random random, Map<String, List<IncomingRequest>> incomingRequestsByPeer, Items items,
						  Recipes recipes) {
		super(interactionHandlers, worldConfiguration, random);
		this.incomingRequestsByPeer = incomingRequestsByPeer;
		this.items = items;
		this.recipes = recipes;
	}

	@Override
	public void process(int entity) {
		if (interactMapper.has(entity) || pathMapper.has(entity) || craftMapper.has(entity)) {
			return;
		}

		VastPeer peer = observerMapper.get(observedMapper.get(entity).observerEntity).peer;
		float roll = random.nextFloat() * 100f;
		if (roll <= 0.2f) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.SET_HOME)));
		} else if (roll <= 2f) {
			Recipe recipe;
			if (random.nextFloat() <= 0.5f) {
				recipe = recipes.getRecipeByItemName("Axe");
			} else {
				recipe = recipes.getRecipeByItemName("Pickaxe");
			}
			Inventory inventory = inventoryMapper.get(entity);
			if (inventory.has(recipe.getCosts())) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.CRAFT, new DataObject().set(MessageCodes.CRAFT_RECIPE_ID, (byte) recipe.getId()))));
			}
		} else if (roll <= 5f) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));
		} else if (roll <= 10f) {
			Inventory inventory = inventoryMapper.get(entity);
			if (inventory.has(items.getItem("Seed"))) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.PLANT)));
			}
		} else if (roll <= 30f) {
			Inventory inventory = inventoryMapper.get(entity);
			List<Recipe> entityRecipes = recipes.getEntityRecipes();
			Recipe randomEntityRecipe = entityRecipes.get((int) Math.floor(random.nextFloat() * entityRecipes.size()));
			if (inventory.has(randomEntityRecipe.getCosts())) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD_START, new DataObject()
					.set(MessageCodes.BUILD_START_RECIPE_ID, (byte) randomEntityRecipe.getId()))));
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD_CONFIRM)));
			}
		} else if (roll <= 60f) {
			List<Integer> nearbyEntities = getNearbyEntities(entity);
			if (nearbyEntities.size() > 0) {
				int randomIndex = (int) (random.nextFloat() * nearbyEntities.size());
				int randomNearbyEntity = nearbyEntities.get(randomIndex);
				Inventory inventory = inventoryMapper.get(entity);
				boolean skip = false;
				if (typeMapper.has(randomNearbyEntity)) {
					if (typeMapper.get(randomNearbyEntity).type.equals("tree") && !hasItemWithTag(inventory, "axe")) {
						skip = true;
					} else if (typeMapper.get(randomNearbyEntity).type.equals("rock") && !hasItemWithTag(inventory, "pickaxe")) {
						skip = true;
					}
				}
				if (!skip && canInteract(entity, randomNearbyEntity)) {
					addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyEntity))));
				}
			}
		} else {
			Point2f randomMovePosition = getRandomMovePosition(transformMapper.get(entity).position, 2f);
			if (randomMovePosition != null) {
				float[] position = new float[]{randomMovePosition.x, randomMovePosition.y};
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.MOVE, new DataObject().set(MessageCodes.MOVE_POSITION, position))));
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

	private void addIncomingRequest(IncomingRequest incomingRequest) {
		List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(incomingRequest.getPeer().getName());
		if (incomingRequests == null) {
			incomingRequests = new ArrayList<>();
			incomingRequestsByPeer.put(incomingRequest.getPeer().getName(), incomingRequests);
		}
		incomingRequests.add(incomingRequest);
	}
}
