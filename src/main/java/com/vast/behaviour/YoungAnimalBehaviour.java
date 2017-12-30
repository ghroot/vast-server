package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.AI;
import com.vast.component.Follow;
import com.vast.component.Group;
import com.vast.interact.InteractionHandler;

import java.util.List;

public class YoungAnimalBehaviour extends AbstractBehaviour {
	private ComponentMapper<Group> groupMapper;
	private ComponentMapper<Follow> followMapper;

	public YoungAnimalBehaviour(List<InteractionHandler> interactionHandlers) {
		super(interactionHandlers);
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
						followMapper.get(entity).distance = 1.0f + 1.0f * (float) Math.random();
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
