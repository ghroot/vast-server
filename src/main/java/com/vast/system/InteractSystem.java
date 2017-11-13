package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.component.Interact;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InteractSystem extends BaseSystem {
    private static final Logger logger = LoggerFactory.getLogger(InteractSystem.class);

    private ComponentMapper<Player> playerMapper;
    private ComponentMapper<Interact> interactMapper;

    private List<IncomingRequest> incomingRequests;

    public InteractSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    @Override
    protected void processSystem() {
        for (IncomingRequest request : incomingRequests) {
            if (request.getMessage().getCode() == MessageCodes.INTERACT) {
                int playerEntity = getEntityWithPeerName(request.getPeer().getName());
                if (!interactMapper.has(playerEntity)) {
                    interactMapper.create(playerEntity);
                }
                interactMapper.get(playerEntity).entity = (int) request.getMessage().getDataObject().get(MessageCodes.INTERACT_ENTITY_ID).value;
            }
        }
    }

    private int getEntityWithPeerName(String name) {
        IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int entity = entities.get(i);
            Player peer = playerMapper.get(entity);
            if (peer.name.equals(name)) {
                return entity;
            }
        }
        return -1;
    }
}
