package com.vast.property.progress;

import com.artemis.ComponentMapper;
import com.vast.component.Producer;
import com.vast.data.ItemRecipe;
import com.vast.data.Recipe;
import com.vast.data.Recipes;

public class ProducerProgressPropertyHandler extends AbstractProgressPropertyHandler {
	private ComponentMapper<Producer> producerMapper;

	private Recipes recipes;

	public ProducerProgressPropertyHandler(Recipes recipes, int progressThreshold) {
		super(progressThreshold);
		this.recipes = recipes;
	}

	@Override
	public boolean isInterestedIn(int entity) {
		return producerMapper.has(entity);
	}

	@Override
	protected Integer getPropertyData(int entity) {
		Producer producer = producerMapper.get(entity);
		ItemRecipe recipe = recipes.getItemRecipe(producer.recipeId);
		return Math.min((int) Math.floor(100f * producer.time / recipe.getDuration()), 100);
	}
}
