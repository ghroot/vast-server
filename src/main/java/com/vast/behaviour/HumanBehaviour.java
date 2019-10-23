package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.component.*;
import com.vast.data.Buildings;
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
		int roll = (int) (random.nextFloat() * 100);
		if (roll <= 1) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.SET_HOME)));
		} else if (roll <= 3) {
			int itemId;
			if (random.nextFloat() <= 0.5f) {
				itemId = items.getItem("axe").getId();
			} else {
				itemId = items.getItem("pickaxe").getId();
			}
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.CRAFT, new DataObject().set(MessageCodes.CRAFT_ITEM_TYPE, (byte) itemId))));
		} else if (roll <= 7) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));
		} else if (roll <= 15) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.PLANT)));
		} else if (roll <= 20) {
			byte buildingId = (byte) (random.nextFloat() * 3);
			float x = transformMapper.get(entity).position.x;
			float y = transformMapper.get(entity).position.y - 1.0f;
			float[] position = new float[] {x, y};
			float rotation = random.nextFloat() * 360f;
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD, new DataObject()
				.set(MessageCodes.BUILD_TYPE, buildingId)
				.set(MessageCodes.BUILD_POSITION, position)
				.set(MessageCodes.BUILD_ROTATION, rotation))));
		} else if (roll <= 55) {
			List<Integer> nearbyEntities = getNearbyEntities(entity);
			if (nearbyEntities.size() > 0) {
				int randomIndex = (int) (random.nextFloat() * nearbyEntities.size());
				int randomNearbyEntity = nearbyEntities.get(randomIndex);
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyEntity))));
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
