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
	private ComponentMapper<State> stateMapper;
	private ComponentMapper<Build> buildMapper;
	private ComponentMapper<Delete> deleteMapper;

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
	public boolean handlesMessageCode(short messageCode) {
		return messageCode == MessageCodes.BUILD_START || messageCode == MessageCodes.BUILD_MOVE ||
				messageCode == MessageCodes.BUILD_ROTATE || messageCode == MessageCodes.BUILD_CONFIRM ||
				messageCode == MessageCodes.BUILD_CANCEL;
	}

	@Override
	public boolean isOrderComplete(int orderEntity) {
		return !buildMapper.has(orderEntity) && !interactMapper.has(orderEntity);
	}

	@Override
	public void cancelOrder(int orderEntity) {
		interactMapper.remove(orderEntity);
		pathMapper.remove(orderEntity);

		if (buildMapper.has(orderEntity)) {
			deleteMapper.create(buildMapper.get(orderEntity).placeholderEntity).reason = "canceled";
			buildMapper.remove(orderEntity);
		}
	}

	@Override
	public boolean startOrder(int orderEntity, short messageCode, DataObject dataObject) {
		if (messageCode == MessageCodes.BUILD_START) {
			int recipeId = (byte) dataObject.get(MessageCodes.BUILD_START_RECIPE_ID).value;
			Recipe recipe = recipes.getRecipe(recipeId);
			Transform orderTransform = transformMapper.get(orderEntity);

			Point2f buildPosition = new Point2f(orderTransform.position.x, orderTransform.position.y + 3f);
			int buildingPlaceholderEntity = creationManager.createBuildingPlaceholder(recipe.getEntityType(), buildPosition);

			Build build = buildMapper.create(orderEntity);
			build.placeholderEntity = buildingPlaceholderEntity;
			build.recipe = recipe;

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject) {
		if (messageCode == MessageCodes.BUILD_MOVE) {
			int direction = (byte) dataObject.get(MessageCodes.BUILD_MOVE_DIRECTION).value;
			Build build = buildMapper.get(orderEntity);
			Transform buildingPlaceholderTransform = transformMapper.get(build.placeholderEntity);
			switch (direction) {
				case 0:
					buildingPlaceholderTransform.position.y += 0.5f;
					break;
				case 1:
					buildingPlaceholderTransform.position.x += 0.5f;
					break;
				case 2:
					buildingPlaceholderTransform.position.y -= 0.5f;
					break;
				case 3:
					buildingPlaceholderTransform.position.x -= 0.5f;
					break;
			}

			syncMapper.create(build.placeholderEntity).markPropertyAsDirty(Properties.POSITION);

			return true;
		} else if (messageCode == MessageCodes.BUILD_ROTATE) {
			int direction = (byte) dataObject.get(MessageCodes.BUILD_ROTATE_DIRECTION).value;
			Build build = buildMapper.get(orderEntity);
			Transform buildingPlaceholderTransform = transformMapper.get(build.placeholderEntity);
			switch (direction) {
				case 0:
					buildingPlaceholderTransform.rotation += 10f;
					if (buildingPlaceholderTransform.rotation >= 360f) {
						buildingPlaceholderTransform.rotation -= 360f;
					}
					break;
				case 1:
					buildingPlaceholderTransform.rotation -= 10f;
					if (buildingPlaceholderTransform.rotation < 0f) {
						buildingPlaceholderTransform.rotation += 360f;
					}
					break;
			}

			syncMapper.create(build.placeholderEntity).markPropertyAsDirty(Properties.ROTATION);

			return true;
		} else if (messageCode == MessageCodes.BUILD_CONFIRM) {
			Build build = buildMapper.get(orderEntity);
			Inventory inventory = inventoryMapper.get(orderEntity);
			if (inventory.has(build.recipe.getCosts())) {
				inventory.remove(build.recipe.getCosts());
				syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);

				Transform placeholderTransform = transformMapper.get(build.placeholderEntity);

				int buildingEntity = creationManager.createBuilding(build.recipe.getEntityType(),
						placeholderTransform.position, placeholderTransform.rotation, playerMapper.get(orderEntity).name);
				stateMapper.get(buildingEntity).name = "placed";
				createMapper.create(buildingEntity).reason = "built";

				interactMapper.create(orderEntity).entity = buildingEntity;

				deleteMapper.create(build.placeholderEntity);
				buildMapper.remove(orderEntity);

				return true;
			} else {
				eventMapper.create(orderEntity).addEntry("message").setData("I don't have the required materials...").setOwnerPropagation();
				return false;
			}
		} else if (messageCode == MessageCodes.BUILD_CANCEL) {
			return false;
		} else {
			return false;
		}
	}
}
