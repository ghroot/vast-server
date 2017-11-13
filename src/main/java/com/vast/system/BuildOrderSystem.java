package com.vast.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.vast.IncomingRequest;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.Iterator;
import java.util.List;

@Profile(enabled = true, using = Profiler.class)
public class BuildOrderSystem extends AbstractOrderSystem {
    private static final Logger logger = LoggerFactory.getLogger(BuildOrderSystem.class);

    private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Order> orderMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Building> buildingMapper;

    private List<IncomingRequest> incomingRequests;

    private Archetype buildingArcheType;

    public BuildOrderSystem(List<IncomingRequest> incomingRequests) {
        this.incomingRequests = incomingRequests;
    }

	@Override
	protected void initialize() {
		buildingArcheType = new ArchetypeBuilder()
				.add(Type.class)
				.add(Transform.class)
				.add(Spatial.class)
				.add(Collision.class)
				.add(Building.class)
				.build(world);
	}

	@Override
    protected void processSystem() {
		for (Iterator<IncomingRequest> iterator = incomingRequests.iterator(); iterator.hasNext();) {
			IncomingRequest request = iterator.next();
			int playerEntity = getEntityWithPeerName(request.getPeer().getName());
			if (!orderMapper.has(playerEntity) && request.getMessage().getCode() == MessageCodes.BUILD) {
				String type = (String) request.getMessage().getDataObject().get((MessageCodes.BUILD_TYPE)).value;
				float[] position = (float[]) request.getMessage().getDataObject().get(MessageCodes.BUILD_POSITION).value;
				Point2f buildPosition = new Point2f(position[0], position[1]);
				logger.debug("Starting build order for entity {}: {} at {}", playerEntity, type, buildPosition);
				int buildingEntity = world.create(buildingArcheType);
				typeMapper.get(buildingEntity).type = "building";
				transformMapper.get(buildingEntity).position.set(buildPosition);
				collisionMapper.get(buildingEntity).isStatic = true;
				collisionMapper.get(buildingEntity).radius = 0.5f;
				buildingMapper.get(buildingEntity).type = type;
				iterator.remove();
			}
		}
    }
}
