package com.vast.property;

import com.artemis.ComponentMapper;
import com.vast.component.Constructable;
import com.vast.component.Producer;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.Properties;

public class ProgressPropertyHandler extends AbstractPropertyHandler<Integer, Byte> {
	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Producer> producerMapper;

	private Recipes recipes;
	private int progressThreshold;

	public ProgressPropertyHandler(Recipes recipes, int progressThreshold) {
		super(Properties.PROGRESS);
		this.recipes = recipes;
		this.progressThreshold = progressThreshold;
	}

	@Override
	protected boolean isInterestedIn(int entity) {
		return constructableMapper.has(entity) || producerMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		if (constructableMapper.has(entity)) {
			Constructable constructable = constructableMapper.get(entity);
			return Math.min((int) Math.floor(100.0f * constructable.buildTime / constructable.buildDuration), 100);
		} else {
			Producer producer = producerMapper.get(entity);
			Recipe recipe = recipes.getRecipe(producer.recipeId);
			return Math.min((int) Math.floor(100.0f * producer.time / recipe.getDuration()), 100);
		}
	}

	@Override
	protected boolean passedThresholdForSync(int entity, Integer lastSyncedProgress) {
		int progress = getPropertyData(entity);
		if (progress != lastSyncedProgress) {
			if (progress == 100) {
				return true;
			} else {
				int progressDifference = Math.abs(lastSyncedProgress - progress);
				return progressDifference >= progressThreshold;
			}
		} else {
			return false;
		}
	}

	@Override
	protected Byte convertPropertyDataToDataObjectData(Integer propertyData) {
		return propertyData.byteValue();
	}
}
