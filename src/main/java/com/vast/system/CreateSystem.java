package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.*;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class CreateSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreateSystem.class);

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;

	private Map<String, VastPeer> peers;
	private Set<PropertyHandler> propertyHandlers;

	private EventMessage reusableEventMessage;

	public CreateSystem(Map<String, VastPeer> peers, Set<PropertyHandler> propertyHandlers) {
		super(Aspect.all(Create.class, Type.class));
		this.peers = peers;
		this.propertyHandlers = propertyHandlers;

		reusableEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject());
	}

	@Override
	protected void process(int createEntity, Set<Integer> nearbyEntities) {
		for (int nearbyEntity : nearbyEntities) {
			if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity) && knownMapper.has(nearbyEntity)) {
				Set<Integer> knownEntities = knownMapper.get(nearbyEntity).knownEntities;
				if (!knownEntities.contains(createEntity)) {
					VastPeer peer = peers.get(playerMapper.get(nearbyEntity).name);
					String reason = createMapper.get(createEntity).reason;
					logger.debug("Notifying peer {} about new entity {} ({})", peer.getName(), createEntity, reason);
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, createEntity);
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, typeMapper.get(createEntity).type);
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, reason);
					if (playerMapper.has(createEntity)) {
						reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, peer.getName().equals(playerMapper.get(createEntity).name));
					}
					for (PropertyHandler propertyHandler : propertyHandlers) {
						propertyHandler.decorateDataObject(createEntity, reusableEventMessage.getDataObject());
					}
					peer.send(reusableEventMessage);
					knownEntities.add(createEntity);
				}
			}
		}
		createMapper.remove(createEntity);
	}
}
