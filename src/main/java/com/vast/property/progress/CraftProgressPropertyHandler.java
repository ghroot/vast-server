package com.vast.property.progress;

import com.artemis.ComponentMapper;
import com.vast.component.Craft;

public class CraftProgressPropertyHandler extends AbstractProgressPropertyHandler {
	private ComponentMapper<Craft> craftMapper;

	public CraftProgressPropertyHandler(int progressThreshold) {
		super(progressThreshold);
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return craftMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		Craft craft = craftMapper.get(entity);
		return Math.min((int) Math.floor(100f * craft.craftTime / craft.recipe.getDuration()), 100);
	}
}
