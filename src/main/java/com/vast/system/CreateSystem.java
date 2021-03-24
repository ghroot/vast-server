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
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class CreateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreateSystem.class);

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;

	@All({Observer.class, Scan.class})
	private EntitySubscription interestedSubscription;

	private PropertyHandler[] propertyHandlers;

	private EventMessage reusableEventMessage;
	private Set<Byte> reusableAlreadyInterestedProperties;
	private DataObject reusablePropertiesDataObject;

	public CreateSystem(PropertyHandler[] propertyHandlers) {
		super(Aspect.all(Create.class, Type.class).exclude(Invisible.class));
		this.propertyHandlers = propertyHandlers;

		reusableEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED);
		reusableAlreadyInterestedProperties = new UnifiedSet<>();
		reusablePropertiesDataObject = new DataObject();
	}

	@Override
	protected void process(int createEntity) {
		Create create = createMapper.get(createEntity);
		SyncPropagation syncPropagation = syncPropagationMapper.get(createEntity);
		Known known = knownMapper.get(createEntity);

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, createEntity);
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, typeMapper.get(createEntity).type);
		if (subTypeMapper.has(createEntity)) {
			reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_SUB_TYPE, subTypeMapper.get(createEntity).subType);
		} else {
			reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_SUB_TYPE, null);
		}
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, create.reason);
		reusableAlreadyInterestedProperties.clear();
		reusablePropertiesDataObject.clear();
		for (PropertyHandler propertyHandler : propertyHandlers) {
			byte property = propertyHandler.getProperty();
			if (syncPropagation.isNearbyPropagation(propertyHandler.getProperty())) {
				if (!reusableAlreadyInterestedProperties.contains(property) && propertyHandler.isInterestedIn(createEntity)) {
					propertyHandler.decorateDataObject(createEntity, reusablePropertiesDataObject, true);
					reusableAlreadyInterestedProperties.add(property);
				}
			}
		}
		reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_PROPERTIES, reusablePropertiesDataObject);

		IntBag interestedEntities = interestedSubscription.getEntities();
		for (int i = 0; i < interestedEntities.size(); i++) {
			int interestedEntity = interestedEntities.get(i);
			Scan interestedScan = scanMapper.get(interestedEntity);
			if (interestedScan.nearbyEntities.contains(createEntity)) {
				Observer interestedObserver = observerMapper.get(interestedEntity);
				VastPeer peer = interestedObserver.peer;
				logger.debug("Notifying peer {} about new entity {} ({})", peer.getName(), createEntity, create.reason);
				if (avatarMapper.has(createEntity)) {
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, peer.getName().equals(avatarMapper.get(createEntity).name));
				} else {
					reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_OWNER, null);
				}
				peer.send(reusableEventMessage);
				interestedObserver.knowEntities.add(createEntity);
				if (known != null) {
					known.knownByEntities.add(interestedEntity);
				}
			}
		}

		createMapper.remove(createEntity);
	}
}
