package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.component.*;
import com.vast.data.Building;
import com.vast.data.Buildings;
import com.vast.data.CraftableItem;
import com.vast.data.Items;
import com.vast.interact.InteractionHandler;
import com.vast.network.IncomingRequest;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HumanBehaviour extends AbstractBehaviour {
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Craft> craftMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Type> typeMapper;

	private Random random;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;
	private Items items;
	private Buildings buildings;

	public HumanBehaviour(List<InteractionHandler> interactionHandlers, Random random, Map<String, List<IncomingRequest>> incomingRequestsByPeer, Items items, Buildings buildings) {
		super(interactionHandlers);
		this.random = random;
		this.incomingRequestsByPeer = incomingRequestsByPeer;
		this.items = items;
		this.buildings = buildings;
	}

	@Override
	public void process(int entity) {
		if (!activeMapper.has(entity) || interactMapper.has(entity) || pathMapper.has(entity) || craftMapper.has(entity)) {
			return;
		}

		VastPeer peer = activeMapper.get(entity).peer;
		float roll = random.nextFloat() * 100f;
		if (roll <= 0.2f) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.SET_HOME)));
		} else if (roll <= 2f) {
			CraftableItem item;
			if (random.nextFloat() <= 0.5f) {
				item = (CraftableItem) items.getItem("axe");
			} else {
				item = (CraftableItem) items.getItem("pickaxe");
			}
			Inventory inventory = inventoryMapper.get(entity);
			if (inventory.has(item.getCosts())) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.CRAFT, new DataObject().set(MessageCodes.CRAFT_ITEM_TYPE, (byte) item.getId()))));
			}
		} else if (roll <= 5f) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));
		} else if (roll <= 10f) {
			Inventory inventory = inventoryMapper.get(entity);
			if (inventory.has(items.getItem("seed"))) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.PLANT)));
			}
		} else if (roll <= 30f) {
			Inventory inventory = inventoryMapper.get(entity);
			List<Building> allBuildings = buildings.getAllBuildings();
			Building randomBuilding = allBuildings.get((int) Math.floor(random.nextFloat() * allBuildings.size()));
			if (inventory.has(randomBuilding.getCosts())) {
				float x = transformMapper.get(entity).position.x;
				float y = transformMapper.get(entity).position.y - 1.0f;
				float[] position = new float[]{x, y};
				float rotation = random.nextFloat() * 360f;
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD, new DataObject()
					.set(MessageCodes.BUILD_TYPE, (byte) randomBuilding.getId())
					.set(MessageCodes.BUILD_POSITION, position)
					.set(MessageCodes.BUILD_ROTATION, rotation))));
			}
		} else if (roll <= 60f) {
			List<Integer> nearbyEntities = getNearbyEntities(entity);
			if (nearbyEntities.size() > 0) {
				int randomIndex = (int) (random.nextFloat() * nearbyEntities.size());
				int randomNearbyEntity = nearbyEntities.get(randomIndex);
				Inventory inventory = inventoryMapper.get(entity);
				boolean skip = false;
				if (typeMapper.has(randomNearbyEntity)) {
					if (typeMapper.get(randomNearbyEntity).type.equals("tree") && !inventory.has(items.getItem("axe"))) {
						skip = true;
					} else if (typeMapper.get(randomNearbyEntity).type.equals("rock") && !inventory.has(items.getItem("pickaxe"))) {
						skip = true;
					}
				}
				if (!skip && canInteract(entity, randomNearbyEntity)) {
					addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyEntity))));
				}
			}
		} else {
			float x = transformMapper.get(entity).position.x - 2f + random.nextFloat() * 4f;
			float y = transformMapper.get(entity).position.y - 2f + random.nextFloat() * 4f;
			float[] position = new float[] {x, y};
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.MOVE, new DataObject().set(MessageCodes.MOVE_POSITION, position))));
		}
	}

	private void addIncomingRequest(IncomingRequest incomingRequest) {
		List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(incomingRequest.getPeer().getName());
		if (incomingRequests == null) {
			incomingRequests = new ArrayList<IncomingRequest>();
			incomingRequestsByPeer.put(incomingRequest.getPeer().getName(), incomingRequests);
		}
		incomingRequests.add(incomingRequest);
	}
}
