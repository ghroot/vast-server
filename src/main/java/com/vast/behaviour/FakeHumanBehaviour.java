package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.Interact;
import com.vast.component.Path;
import com.vast.component.Player;
import com.vast.component.Transform;
import com.vast.interact.InteractionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FakeHumanBehaviour extends AbstractBehaviour {
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;

	private Map<String, VastPeer> peers;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

	public FakeHumanBehaviour(List<InteractionHandler> interactionHandlers, Map<String, VastPeer> peers, Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
		super(interactionHandlers);
		this.peers = peers;
		this.incomingRequestsByPeer = incomingRequestsByPeer;
	}

	@Override
	public void process(int entity) {
		if (interactMapper.has(entity) || pathMapper.has(entity)) {
			return;
		}

		Player player = playerMapper.get(entity);
		VastPeer peer = peers.get(player.name);
		int roll = (int) (Math.random() * 100);
		if (roll <= 5) {
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));
		} else if (roll <= 15) {
			byte buildingType = (byte) (Math.random() * 4);
			float x = transformMapper.get(entity).position.x;
			float y = transformMapper.get(entity).position.y + 1.0f;
			float[] buildPosition = new float[] {x, y};
			addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD, new DataObject().set(MessageCodes.BUILD_TYPE, buildingType).set(MessageCodes.BUILD_POSITION, buildPosition))));
		} else if (roll <= 50) {
			List<Integer> nearbyInteractableEntities = getNearbyInteractableEntities(entity);
			if (nearbyInteractableEntities.size() > 0) {
				int randomIndex = (int) (Math.random() * nearbyInteractableEntities.size());
				int randomNearbyInteractableEntity = nearbyInteractableEntities.get(randomIndex);
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyInteractableEntity))));
			}
		} else {
			float x = transformMapper.get(entity).position.x - 2.0f + (float) Math.random() * 4.0f;
			float y = transformMapper.get(entity).position.y - 2.0f + (float) Math.random() * 4.0f;
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
