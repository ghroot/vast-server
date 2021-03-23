package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Observer;
import com.vast.component.OrderQueue;
import com.vast.network.IncomingRequest;
import com.vast.network.MessageCodes;
import com.vast.order.request.observer.AttachObserverOrderRequest;
import com.vast.order.request.observer.BuildStartOrderRequest;
import com.vast.order.request.observer.MoveObserverOrderRequest;

import javax.vecmath.Point2f;
import java.util.List;
import java.util.Map;

public class IncomingObserverOrderSystem extends IteratingSystem {
    private ComponentMapper<Observer> observerMapper;
    private ComponentMapper<OrderQueue> orderQueueMapper;

    private Map<String, List<IncomingRequest>> incomingRequestsByPeer;

    public IncomingObserverOrderSystem(Map<String, List<IncomingRequest>> incomingRequestsByPeer) {
        super(Aspect.all(Observer.class));
        this.incomingRequestsByPeer = incomingRequestsByPeer;
    }

    @Override
    protected void process(int observerEntity) {
        Observer observer = observerMapper.get(observerEntity);

        if (incomingRequestsByPeer.containsKey(observer.peer.getName())) {
            List<IncomingRequest> incomingRequests = incomingRequestsByPeer.get(observer.peer.getName());
            if (incomingRequests.size() > 0) {
                IncomingRequest lastIncomingRequest = incomingRequests.get(incomingRequests.size() - 1);

                if (lastIncomingRequest.getMessage().getCode() == MessageCodes.MOVE_OBSERVER) {
                    float[] position = (float[]) lastIncomingRequest.getMessage().getDataObject().get(MessageCodes.MOVE_OBSERVER_POSITION).value;
                    orderQueueMapper.create(observerEntity).requests.add(new MoveObserverOrderRequest(new Point2f(position[0], position[1])));
                } else if (lastIncomingRequest.getMessage().getCode() == MessageCodes.ATTACH_OBSERVER) {
                    orderQueueMapper.create(observerEntity).requests.add(new AttachObserverOrderRequest());
                } else if (lastIncomingRequest.getMessage().getCode() == MessageCodes.BUILD_START) {
                    orderQueueMapper.create(observerEntity).requests.add(new BuildStartOrderRequest(
                            (byte) lastIncomingRequest.getMessage().getDataObject().get(MessageCodes.BUILD_START_RECIPE_ID).value)
                    );
                }

                incomingRequests.clear();
            }
        }
    }
}
