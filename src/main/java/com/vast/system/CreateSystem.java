package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.Create;
import com.vast.component.Interactable;
import com.vast.component.Transform;
import com.vast.component.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class CreateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreateSystem.class);

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interactable> interactableMapper;

	private Map<String, VastPeer> peers;
	private Map<String, Set<Integer>> knownEntitiesByPeer;
	private Map<String, Set<Integer>> nearbyEntitiesByPeer;

	private float[] reusablePosition;

	public CreateSystem(Map<String, VastPeer> peers, Map<String, Set<Integer>> knownEntitiesByPeer, Map<String, Set<Integer>> nearbyEntitiesByPeer) {
		super(Aspect.all(Create.class, Transform.class, Type.class));
		this.peers = peers;
		this.knownEntitiesByPeer = knownEntitiesByPeer;
		this.nearbyEntitiesByPeer = nearbyEntitiesByPeer;

		reusablePosition = new float[2];
	}

	@Override
	protected void process(int createEntity) {
		for (VastPeer peer : peers.values()) {
			Set<Integer> nearbyEntities = nearbyEntitiesByPeer.get(peer.getName());
			Set<Integer> knownEntities = knownEntitiesByPeer.get(peer.getName());
			if (nearbyEntities.contains(createEntity) && !knownEntities.contains(createEntity)) {
				String reason = createMapper.get(createEntity).reason;
				logger.debug("Notifying peer {} about new entity {} ({})", peer.getName(), createEntity, reason);
				Type type = typeMapper.get(createEntity);
				Transform transform = transformMapper.get(createEntity);
				reusablePosition[0] = transform.position.x;
				reusablePosition[1] = transform.position.y;
				peer.send(new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject()
								.set(MessageCodes.ENTITY_CREATED_ENTITY_ID, createEntity)
								.set(MessageCodes.ENTITY_CREATED_TYPE, type.type)
								.set(MessageCodes.ENTITY_CREATED_POSITION, reusablePosition)
								.set(MessageCodes.ENTITY_CREATED_REASON, reason)
								.set(MessageCodes.ENTITY_CREATED_INTERACTABLE, interactableMapper.has(createEntity))),
						SendOptions.ReliableSend);
				knownEntities.add(createEntity);
			}
		}
		createMapper.remove(createEntity);
	}
}
