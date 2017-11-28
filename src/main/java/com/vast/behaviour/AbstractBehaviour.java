package com.vast.behaviour;

import com.artemis.ComponentMapper;
import com.vast.component.Disabled;
import com.vast.component.Interactable;
import com.vast.component.Scan;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBehaviour implements Behaviour {
	protected ComponentMapper<Scan> scanMapper;
	protected ComponentMapper<Interactable> interactableMapper;
	private ComponentMapper<Disabled> disabledMapper;

	private List<Integer> reusableNearbyInteractableEntities;

	public AbstractBehaviour() {
		reusableNearbyInteractableEntities = new ArrayList<Integer>();
	}

	protected List<Integer> getNearbyInteractableEntities(int entity) {
		Scan scan = scanMapper.get(entity);
		reusableNearbyInteractableEntities.clear();
		for (int nearbyEntity : scan.nearbyEntities) {
			if (nearbyEntity != entity && interactableMapper.has(nearbyEntity) && !disabledMapper.has(nearbyEntity)) {
				reusableNearbyInteractableEntities.add(nearbyEntity);
			}
		}
		return reusableNearbyInteractableEntities;
	}
}
