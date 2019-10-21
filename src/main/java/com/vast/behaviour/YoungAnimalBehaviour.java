package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.AI;
import com.vast.component.Follow;
import com.vast.component.Group;
import com.vast.interact.InteractionHandler;

import java.util.List;
import java.util.Random;

public class YoungAnimalBehaviour extends AbstractBehaviour {
	private ComponentMapper<Group> groupMapper;
	private ComponentMapper<Follow> followMapper;

	private Random random;

	public YoungAnimalBehaviour(List<InteractionHandler> interactionHandlers, Random random) {
		super(interactionHandlers);
		this.random = random;
	}

	@Override
	public void process(int entity) {
		AI ai = aiMapper.get(entity);
		Group group = groupMapper.get(entity);

		if (ai.state.equals("idling")) {
			if (scanMapper.has(entity)) {
				for (int nearbyEntity : getNearbyEntities(entity)) {
					if (groupMapper.has(nearbyEntity) && groupMapper.get(nearbyEntity).id == group.id) {
						followMapper.create(entity).entity = nearbyEntity;
						followMapper.get(entity).distance = 1f + 1f * random.nextFloat();
						ai.state = "following";
						break;
					}
				}
				scanMapper.remove(entity);
			} else {
				scanMapper.create(entity);
			}
		}
	}
}
