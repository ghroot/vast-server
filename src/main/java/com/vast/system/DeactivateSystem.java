package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.network.Properties;
import com.vast.network.VastPeer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DeactivateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(DeactivateSystem.class);

	private ComponentMapper<Avatar> avatarMapper;
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Known> knownMapper;

	private Map<String, VastPeer> peers;

	public DeactivateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Avatar.class, Observed.class));
		this.peers = peers;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int avatarEntity) {
		Avatar avatar = avatarMapper.get(avatarEntity);
		Observed observed = observedMapper.get(avatarEntity);
		Observer observer = observerMapper.get(observed.observerEntity);
		if (!peers.containsKey(avatar.name) || peers.get(avatar.name).getId() != observer.peer.getId()) {
			logger.info("Deactivating peer entity: {} for {} ({})", avatarEntity, avatar.name, observer.peer.getId());

			IntBag knowEntitiesBag = observer.knowEntities;
			int[] knowEntities = knowEntitiesBag.getData();
			for (int i = 0, size = knowEntitiesBag.size(); i < size; ++i) {
				int knowEntity = knowEntities[i];
				knownMapper.get(knowEntity).knownByEntities.removeValue(observed.observerEntity);
			}

			observedMapper.remove(avatarEntity);
			syncMapper.create(avatarEntity).markPropertyAsDirty(Properties.ACTIVE);

			world.delete(observed.observerEntity);
		}
	}
}
