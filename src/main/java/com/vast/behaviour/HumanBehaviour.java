package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.*;
import com.vast.data.Item;
import com.vast.data.Items;
import com.vast.data.Recipes;
import com.vast.data.WorldConfiguration;
import com.vast.interact.InteractionHandler;
import com.vast.network.IncomingRequest;
import com.vast.order.request.avatar.EmoteOrderRequest;
import com.vast.order.request.avatar.InteractOrderRequest;
import com.vast.order.request.avatar.MoveOrderRequest;
import com.vast.order.request.avatar.SetHomeOrderRequest;

import javax.vecmath.Point2f;
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
	private ComponentMapper<OrderQueue> orderQueueMapper;

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
	public void process(int avatarEntity) {
		if (interactMapper.has(avatarEntity) || pathMapper.has(avatarEntity) || craftMapper.has(avatarEntity)) {
			return;
		}

//		VastPeer peer = observerMapper.get(observedMapper.get(entity).observerEntity).peer;
		float roll = random.nextFloat() * 100f;
		if (roll <= 0.2f) {
			// Old way
//			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.SET_HOME)));

			// New way
			orderQueueMapper.create(avatarEntity).requests.add(new SetHomeOrderRequest());
		} else if (roll <= 2f) {
//			Recipe recipe;
//			if (random.nextFloat() <= 0.5f) {
//				recipe = recipes.getRecipeByItemName("Axe");
//			} else {
//				recipe = recipes.getRecipeByItemName("Pickaxe");
//			}
//			Inventory inventory = inventoryMapper.get(entity);
//			if (inventory.has(recipe.getCosts())) {
//				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.CRAFT, new DataObject().set(MessageCodes.CRAFT_RECIPE_ID, (byte) recipe.getId()))));
//			}
		} else if (roll <= 5f) {
			// Old way
//			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));

			// New way
			orderQueueMapper.create(avatarEntity).requests.add(new EmoteOrderRequest(0));
		} else if (roll <= 10f) {
//			Inventory inventory = inventoryMapper.get(entity);
//			if (inventory.has(items.getItem("Seed"))) {
//				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.PLANT)));
//			}
		} else if (roll <= 30f) {
//			Inventory inventory = inventoryMapper.get(entity);
//			List<Recipe> entityRecipes = recipes.getEntityRecipes();
//			Recipe randomEntityRecipe = entityRecipes.get((int) Math.floor(random.nextFloat() * entityRecipes.size()));
//			if (inventory.has(randomEntityRecipe.getCosts())) {
//				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD_START, new DataObject()
//					.set(MessageCodes.BUILD_START_RECIPE_ID, (byte) randomEntityRecipe.getId()))));
//				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD_CONFIRM)));
//			}
		} else if (roll <= 60f) {
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
					// Old way
//					addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyEntity))));

					// Direct way
//					interactMapper.create(avatarEntity).entity = randomNearbyEntity;

					// New way
					orderQueueMapper.create(avatarEntity).requests.add(new InteractOrderRequest(randomNearbyEntity));
				}
			}
		} else {
			Point2f randomMovePosition = getRandomMovePosition(transformMapper.get(avatarEntity).position, 2f);
			if (randomMovePosition != null) {
				// Old way
//				float[] position = new float[]{randomMovePosition.x, randomMovePosition.y};
//				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.MOVE, new DataObject().set(MessageCodes.MOVE_POSITION, position))));

				// Direct way
//				pathMapper.create(entity).targetPosition.set(randomMovePosition);

				// New way
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

//	private void addIncomingRequest(IncomingRequest incomingRequest) {
//		List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(incomingRequest.getPeer().getName());
//		if (incomingRequests == null) {
//			incomingRequests = new ArrayList<>();
//			incomingRequestsByPeer.put(incomingRequest.getPeer().getName(), incomingRequests);
//		}
//		incomingRequests.add(incomingRequest);
//	}
}
