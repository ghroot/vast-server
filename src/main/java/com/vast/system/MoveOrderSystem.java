package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.utils.IntBag;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.component.Order;
import com.vast.component.Path;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.Iterator;
import java.util.List;

@Profile(enabled = true, using = Profiler.class)
public class MoveOrderSystem extends AbstractOrderSystem {
    private static final Logger logger = LoggerFactory.getLogger(MoveOrderSystem.class);

    private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Order> orderMapper;
    private ComponentMapper<Path> pathMapper;

    private List<IncomingRequest> incomingRequests;

    public MoveOrderSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    @Override
    protected void processSystem() {
		IntBag orderEntities = world.getAspectSubscriptionManager().get(Aspect.one(Order.class)).getEntities();
		for (int i = orderEntities.size() - 1; i >= 0; i--) {
			int orderEntity = orderEntities.get(i);
			if (orderMapper.get(orderEntity).type == Order.Type.MOVE && !pathMapper.has(orderEntity)) {
				logger.debug("Move order completed for entity {}", orderEntity);
				orderMapper.remove(orderEntity);
			}
		}

		for (Iterator<IncomingRequest> iterator = incomingRequests.iterator(); iterator.hasNext();) {
			IncomingRequest request = iterator.next();
			int playerEntity = getEntityWithPeerName(request.getPeer().getName());
			if (orderMapper.has(playerEntity)) {
				if (orderMapper.get(playerEntity).type == Order.Type.MOVE) {
					logger.debug("Canceling move order for entity {}", playerEntity);
					orderMapper.remove(playerEntity);
					pathMapper.remove(playerEntity);
				}
			} else if (request.getMessage().getCode() == MessageCodes.MOVE) {
				if (!pathMapper.has(playerEntity)) {
					pathMapper.create(playerEntity);
				}
				float[] position = (float[]) request.getMessage().getDataObject().get(MessageCodes.MOVE_POSITION).value;
				pathMapper.get(playerEntity).targetPosition = new Point2f(position[0], position[1]);
				orderMapper.create(playerEntity).type = Order.Type.MOVE;
				logger.debug("Starting move order for entity {}", playerEntity);
				iterator.remove();
			}
		}
    }
}
