package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

@Profile(enabled = true, using = Profiler.class)
public class InteractOrderSystem extends AbstractOrderSystem {
    private static final Logger logger = LoggerFactory.getLogger(InteractOrderSystem.class);

    private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Order> orderMapper;
    private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Path> pathMapper;

    private List<IncomingRequest> incomingRequests;

    public InteractOrderSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    @Override
    protected void processSystem() {
		IntBag orderEntities = world.getAspectSubscriptionManager().get(Aspect.one(Order.class)).getEntities();
		for (int i = orderEntities.size() - 1; i >= 0; i--) {
			int orderEntity = orderEntities.get(i);
			if (orderMapper.get(orderEntity).type == Order.Type.INTERACT && !interactMapper.has(orderEntity)) {
				logger.debug("Interact order completed for entity {}", orderEntity);
				orderMapper.remove(orderEntity);
			}
		}

		for (Iterator<IncomingRequest> iterator = incomingRequests.iterator(); iterator.hasNext();) {
			IncomingRequest request = iterator.next();
			int playerEntity = getEntityWithPeerName(request.getPeer().getName());
			if (orderMapper.has(playerEntity)) {
				if (orderMapper.get(playerEntity).type == Order.Type.INTERACT) {
					logger.debug("Canceling interact order for entity {}", playerEntity);
					orderMapper.remove(playerEntity);
					interactMapper.remove(playerEntity);
					pathMapper.remove(playerEntity);
				}
			} else if (request.getMessage().getCode() == MessageCodes.INTERACT) {
				int otherEntity = (int) request.getMessage().getDataObject().get(MessageCodes.INTERACT_ENTITY_ID).value;
				if (interactableMapper.has(otherEntity)) {
					if (isInteractableEntityBeingInteractedWith(otherEntity)) {
						logger.debug("Player entity {} tried to interact with busy entity {}", playerEntity, otherEntity);
					} else {
						if (!interactMapper.has(playerEntity)) {
							interactMapper.create(playerEntity);
						}
						interactMapper.get(playerEntity).entity = otherEntity;
						orderMapper.create(playerEntity).type = Order.Type.INTERACT;
						logger.debug("Starting interact order for entity {}", playerEntity);
					}
				} else {
					logger.debug("Player entity {} tried to interact with non-interactable entity {}", playerEntity, otherEntity);
				}
				iterator.remove();
			}
		}
    }

    private boolean isInteractableEntityBeingInteractedWith(int interactableEntity) {
		IntBag interactionEntities = world.getAspectSubscriptionManager().get(Aspect.one(Interact.class)).getEntities();
		for (int i = 0; i < interactionEntities.size(); i++) {
			int interactionEntity = interactionEntities.get(i);
			if (interactMapper.get(interactionEntity).entity == interactableEntity) {
				return true;
			}
		}
		return false;
	}
}
