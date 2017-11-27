package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.VastPeer;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AISystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Player> playerMapper;

	private List<Integer> reusableNearbyInteractableEntities;
	private Map<String, VastPeer> peers;
	private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

	public AISystem(Map<String, VastPeer> peers, Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
		super(Aspect.all(AI.class),5.0f);
		this.peers = peers;
		this.incomingRequestsByPeer = incomingRequestsByPeer;

		reusableNearbyInteractableEntities = new ArrayList<Integer>();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int aiEntity) {
		if (playerMapper.has(aiEntity)) {
			Player player = playerMapper.get(aiEntity);
			VastPeer peer = peers.get(player.name);
			int roll = (int) (Math.random() * 100);
			if (roll <= 5) {
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.EMOTE, new DataObject().set(MessageCodes.EMOTE_TYPE, (byte) 0))));
			} else if (roll <= 15) {
				byte buildingType = (byte) (Math.random() * 4);
				float x = transformMapper.get(aiEntity).position.x;
				float y = transformMapper.get(aiEntity).position.y + 1.0f;
				float[] buildPosition = new float[] {x, y};
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.BUILD, new DataObject().set(MessageCodes.BUILD_TYPE, buildingType).set(MessageCodes.BUILD_POSITION, buildPosition))));
			} else if (roll <= 50) {
				List<Integer> nearbyInteractableEntities = getNearbyInteractableEntities(aiEntity);
				if (nearbyInteractableEntities.size() > 0) {
					int randomIndex = (int) (Math.random() * nearbyInteractableEntities.size());
					int randomNearbyInteractableEntity = nearbyInteractableEntities.get(randomIndex);
					addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.INTERACT, new DataObject().set(MessageCodes.INTERACT_ENTITY_ID, randomNearbyInteractableEntity))));
				}
			} else {
				float x = transformMapper.get(aiEntity).position.x - 2.0f + (float) Math.random() * 4.0f;
				float y = transformMapper.get(aiEntity).position.y - 2.0f + (float) Math.random() * 4.0f;
				float[] position = new float[] {x, y};
				addIncomingRequest(new IncomingRequest(peer, new RequestMessage(MessageCodes.MOVE, new DataObject().set(MessageCodes.MOVE_POSITION, position))));
			}
		} else if (scanMapper.has(aiEntity)) {
			List<Integer> nearbyInteractableEntities = getNearbyInteractableEntities(aiEntity);
			if (nearbyInteractableEntities.size() > 0) {
				int randomIndex = (int) (Math.random() * nearbyInteractableEntities.size());
				int randomNearbyInteractableEntity = nearbyInteractableEntities.get(randomIndex);
				interactMapper.create(aiEntity).entity = randomNearbyInteractableEntity;
			} else {
				pathMapper.create(aiEntity).targetPosition = new Point2f(transformMapper.get(aiEntity).position);
				pathMapper.create(aiEntity).targetPosition.add(new Point2f((float) (-2.0f + Math.random() * 4.0f), (float) (-2.0f + Math.random() * 4.0f)));
			}
			scanMapper.remove(aiEntity);
		} else {
			scanMapper.create(aiEntity);
		}
	}

	private List<Integer> getNearbyInteractableEntities(int entity) {
		Scan scan = scanMapper.get(entity);
		reusableNearbyInteractableEntities.clear();
		for (int nearbyEntity : scan.nearbyEntities) {
			if (nearbyEntity != entity && interactableMapper.has(nearbyEntity)) {
				reusableNearbyInteractableEntities.add(nearbyEntity);
			}
		}
		return reusableNearbyInteractableEntities;
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
