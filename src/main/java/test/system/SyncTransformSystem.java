package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.nhnent.haste.framework.ClientPeer;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import test.MessageCodes;
import test.MyPeer;
import test.component.SyncTransformComponent;
import test.component.TransformComponent;

import java.util.List;

public class SyncTransformSystem extends IntervalIteratingSystem {
    private ComponentMapper<TransformComponent> transformComponentMapper;
    private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;

    private List<MyPeer> peers;

    public SyncTransformSystem(List<MyPeer> peers) {
        super(Aspect.all(TransformComponent.class, SyncTransformComponent.class), 100);
        this.peers = peers;
    }

    @Override
    protected void inserted(int entity) {
        syncTransformComponentMapper.get(entity).lastSyncedPosition.set(transformComponentMapper.get(entity).position);
    }

    @Override
    protected void process(int entity) {
        TransformComponent transformComponent = transformComponentMapper.get(entity);
        SyncTransformComponent syncTransformComponent = syncTransformComponentMapper.get(entity);

        if (!transformComponent.position.equals(syncTransformComponent.lastSyncedPosition)) {
            EventMessage positionMessage = new EventMessage(MessageCodes.SET_POSITION, new DataObject()
                    .set(MessageCodes.SET_POSITION_ENTITY_ID, entity)
                    .set(MessageCodes.SET_POSITION_POSITION, new float[] {transformComponent.position.x, transformComponent.position.y}));
            for (ClientPeer peer : peers) {
                peer.send(positionMessage, SendOptions.ReliableSend);
            }
            syncTransformComponent.lastSyncedPosition.set(transformComponent.position);
        }
    }
}
