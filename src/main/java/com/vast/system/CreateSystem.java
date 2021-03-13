package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.*;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CreateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreateSystem.class);

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	@All({Active.class, Scan.class})
	private EntitySubscription interestedSubscription;

	private PropertyHandler[] propertyHandlers;

	private EventMessage reusableEventMessage;
	private Set<Byte> reusableAlreadyInterestedProperties;
	private DataObject reusablePropertiesDataObject;

	public CreateSystem(PropertyHandler[] propertyHandlers) {
		super(Aspect.all(Create.class, Type.class));
		this.propertyHandlers = propertyHandlers;

		reusableEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED);
		reusableAlreadyInterestedProperties = new HashSet<>();
		reusablePropertiesDataObject = new DataObject();
	}

	@Override
	protected void process(int createEntity) {
		Known known = knownMapper.get(createEntity);
		SyncPropagation syncPropagation = syncPropagationMapper.get(createEntity);
		IntBag interestedEntities = interestedSubscription.getEntities();
		for (int i = 0; i < interestedEntities.size(); i++) {
			int interestedEntity = interestedEntities.get(i);
			Active interestedActive = activeMapper.get(interestedEntity);
			Scan interestedScan = scanMapper.get(interestedEntity);
			if (interestedScan.nearbyEntities.contains(createEntity)) {
				VastPeer peer = interestedActive.peer;
				String reason = createMapper.get(createEntity).reason;
				logger.debug("Notifying peer {} about new entity {} ({})", peer.getName(), createEntity, reason);
				reusableEventMessage.getDataObject().clear();
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, createEntity);
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, typeMapper.get(createEntity).type);
				if (subTypeMapper.has(createEntity)) {
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_SUB_TYPE, subTypeMapper.get(createEntity).subType);
				}
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, reason);
				if (playerMapper.has(createEntity)) {
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, peer.getName().equals(playerMapper.get(createEntity).name));
				}
				reusableAlreadyInterestedProperties.clear();
				reusablePropertiesDataObject.clear();
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_PROPERTIES, reusablePropertiesDataObject);
				for (PropertyHandler propertyHandler : propertyHandlers) {
					byte property = propertyHandler.getProperty();
					if (interestedEntity == createEntity || syncPropagation.isNearbyPropagation(propertyHandler.getProperty())) {
						if (!reusableAlreadyInterestedProperties.contains(property) && propertyHandler.isInterestedIn(createEntity)) {
							propertyHandler.decorateDataObject(createEntity, reusablePropertiesDataObject, true);
							reusableAlreadyInterestedProperties.add(property);
						}
					}
				}
				peer.send(reusableEventMessage);
				interestedActive.knowEntities.add(createEntity);
				if (known != null) {
					known.knownByEntities.add(interestedEntity);
				}
			}
		}

		createMapper.remove(createEntity);
	}
}
