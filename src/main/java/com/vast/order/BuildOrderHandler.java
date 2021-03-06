package com.vast.order;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.*;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.Properties;
import com.vast.network.MessageCodes;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class BuildOrderHandler implements OrderHandler {
	private World world;

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;

	private Recipes recipes;

	private CreationManager creationManager;

	public BuildOrderHandler(Recipes recipes) {
		this.recipes = recipes;
	}

	@Override
	public void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public short getMessageCode() {
		return MessageCodes.BUILD;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !interactMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		interactMapper.remove(orderEntity);
		pathMapper.remove(orderEntity);
	}

	@Override
	public boolean startOrder(int orderEntity, DataObject dataObject) {
		Inventory inventory = inventoryMapper.get(orderEntity);
		int recipeId = (byte) dataObject.get(MessageCodes.BUILD_RECIPE_ID).value;
		float[] position = (float[]) dataObject.get(MessageCodes.BUILD_POSITION).value;
		float rotation = (float) dataObject.get(MessageCodes.BUILD_ROTATION).value;
		Recipe recipe = recipes.getRecipe(recipeId);
		if (inventory.has(recipe.getCosts())) {
			inventory.remove(recipe.getCosts());
			syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

			Point2f buildPosition = new Point2f(position[0], position[1]);
			int buildingEntity = creationManager.createBuilding(recipe.getEntityType(), buildPosition, rotation,
					playerMapper.get(orderEntity).name);
			createMapper.create(buildingEntity).reason = "built";

			interactMapper.create(orderEntity).entity = buildingEntity;

			return true;
		} else {
			eventMapper.create(orderEntity).addEntry("message").setData("I don't have the required materials...").setOwnerPropagation();
			return false;
		}
	}
}
