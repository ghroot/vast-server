package test.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.IncomingRequest;
import test.MessageCodes;
import test.component.PathComponent;
import test.component.PeerComponent;

import javax.vecmath.Point2f;
import java.util.List;

public class PathAssignSystem extends BaseSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathAssignSystem.class);

    private ComponentMapper<PeerComponent> peerComponentMapper;
    private ComponentMapper<PathComponent> pathComponentMapper;

    private List<IncomingRequest> incomingRequests;

    public PathAssignSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

    @Override
    protected void processSystem() {
        for (IncomingRequest request : incomingRequests) {
            if (request.getMessage().getCode() == MessageCodes.SET_PATH) {
                int peerEntity = getEntityWithPeerName(request.getPeer().getName());
                if (!pathComponentMapper.has(peerEntity)) {
                    pathComponentMapper.create(peerEntity);
                }
                float[] position = (float[]) request.getMessage().getDataObject().get(MessageCodes.SET_PATH_POSITION).value;
                logger.info("Setting path for entity {}: {}, {}", peerEntity, position[0], position[1]);
                pathComponentMapper.get(peerEntity).targetPosition = new Point2f(position[0], position[1]);
            }
        }
    }

    private int getEntityWithPeerName(String name) {
        EntitySubscription subscription = world.getAspectSubscriptionManager().get(Aspect.all(PeerComponent.class));
        IntBag entities = subscription.getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int entity = entities.get(i);
            PeerComponent peerComponent = peerComponentMapper.get(entity);
            if (peerComponent.name.equals(name)) {
                return entity;
            }
        }
        return -1;
    }
}
