package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.component.Path;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.List;

public class PathAssignSystem extends BaseSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathAssignSystem.class);

    private ComponentMapper<Player> playerMapper;
    private ComponentMapper<Path> pathMapper;

    private List<IncomingRequest> incomingRequests;

    public PathAssignSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    @Override
    protected void processSystem() {
        for (IncomingRequest request : incomingRequests) {
            if (request.getMessage().getCode() == MessageCodes.SET_PATH) {
                int playerEntity = getEntityWithPeerName(request.getPeer().getName());
                if (!pathMapper.has(playerEntity)) {
                    pathMapper.create(playerEntity);
                }
                float[] position = (float[]) request.getMessage().getDataObject().get(MessageCodes.SET_PATH_POSITION).value;
                logger.debug("Setting path for entity {}: {}, {}", playerEntity, position[0], position[1]);
                pathMapper.get(playerEntity).targetPosition = new Point2f(position[0], position[1]);
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
