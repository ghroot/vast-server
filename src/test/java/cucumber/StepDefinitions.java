package cucumber;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.VastWorld;
import com.vast.component.*;
import com.vast.data.Items;
import com.vast.data.Recipes;
import com.vast.data.WorldConfiguration;
import com.vast.network.VastServerApplication;
import com.vast.order.request.avatar.InteractOrderRequest;
import com.vast.order.request.avatar.MoveOrderRequest;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Duration;
import org.junit.Assert;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

public class StepDefinitions {
    private VastWorld world;
    private Thread worldThread;

    private Map<String, Integer> designations;

    @Before
    public void setUp() {
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss");
        designations = new HashMap<>();
    }

    @After
    public void tearDown(Scenario scenario){
        world.destroy();
    }

    private void createWorld(int width, int height, long seed, String snapshotFile) {
        VastServerApplication serverApplication = mock(VastServerApplication.class);

        WorldConfiguration worldConfiguration = new WorldConfiguration(width, height);
        Items items = new Items("items.json");
        Recipes recipes = new Recipes("recipes.json", items);

        world = new VastWorld(serverApplication, snapshotFile, new Random(seed), true,
                null, worldConfiguration, items, recipes);

        worldThread = new Thread(world, "World");
        worldThread.setPriority(Thread.MAX_PRIORITY);
        worldThread.start();
    }

    @Given("a world with size {int} x {int} and seed {long}")
    public void givenWorldWithSize(int width, int height, long seed) {
        createWorld(width, height, seed, null);
    }

    @Given("an empty world with size {int} x {int} and seed {long}")
    public void givenEmptyWorldWithSize(int width, int height, long seed) {
        createWorld(width, height, seed, "snapshot_empty.json");
    }

    @Given("an empty world")
    public void givenEmptyWorld() {
        createWorld(100, 100, 0, "snapshot_empty.json");
    }

    @Given("a {string} at {float}, {float}")
    public int createEntity(String name, float x, float y) {
        if (name.equals("tree")) {
            return world.getCreationManager().createTree(new Point2f(x, y), false);
        } else if (name.equals("rock")) {
            return world.getCreationManager().createRock(new Point2f(x, y));
        } else if (name.equals("factory")) {
            int buildingEntity =  world.getCreationManager().createBuilding(name, new Point2f(x, y), 0f, "player");
            world.getComponentMapper(Constructable.class).remove(buildingEntity);
            return buildingEntity;
        }

        return -1;
    }

    @Given("a {string} at {float}, {float} owned by player {string}")
    public int createEntity(String name, float x, float y, String playerName) {
        int entity = createEntity(name, x, y);
        if (world.getComponentMapper(Owner.class).has(entity)) {
            world.getComponentMapper(Owner.class).get(entity).name = playerName;
        }

        return entity;
    }

    @Given("a {string} at {float}, {float} called {string}")
    public int createEntityAtWithDesignation(String name, float x, float y, String designation) {
        int entity = createEntity(name, x, y);
        if (entity != -1) {
            designations.put(designation, entity);
        }

        return entity;
    }

    @Given("a {string} at {float}, {float} owned by player {string} called {string}")
    public int createEntityAtWithDesignation(String name, float x, float y, String playerName, String designation) {
        int entity = createEntity(name, x, y, playerName);
        if (entity != -1) {
            designations.put(designation, entity);
        }

        return entity;
    }

    @Given("a player {string}")
    public int playerConnects(String playerName) {
        int avatarEntity = world.getCreationManager().createAvatar(playerName, 0, null);
        designations.put(playerName, avatarEntity);

        return avatarEntity;
    }

    @Given("a player {string} at position {float}, {float}")
    public void playerConnectsAtPosition(String playerName, float x, float y) {
        int avatarEntity = playerConnects(playerName);
        ComponentMapper<Transform> transformMapper = world.getComponentMapper(Transform.class);
        transformMapper.get(avatarEntity).position.set(x, y);
    }

    @Given("player {string} has {int} of item {string}")
    public void playerHasItem(String playerName, int amount, String itemName) {
        world.getComponentMapper(Inventory.class).get(designations.get(playerName)).add(
                world.getItems().getItem(itemName).getId(), amount);
    }

    @Given("the object of type {string} closest to player {string} is designated {string}")
    public void designate(String type, String playerName, String designation) {
        int closestEntity = -1;
        float closestDistance2 = Float.MAX_VALUE;
        ComponentMapper<Type> typeMapper = world.getComponentMapper(Type.class);
        ComponentMapper<Transform> transformMapper = world.getComponentMapper(Transform.class);
        int playerEntity = designations.get(playerName);
        Point2f playerPosition = transformMapper.get(playerEntity).position;
        int[] entities = world.getEntities(Aspect.all(Type.class));
        for (int entity : entities) {
            if (entity != playerEntity && typeMapper.get(entity).type.equals(type)) {
                Point2f position = transformMapper.get(entity).position;
                float distance2 = playerPosition.distanceSquared(position);
                if (distance2 < closestDistance2) {
                    closestEntity = entity;
                    closestDistance2 = distance2;
                }
            }
        }

        if (closestEntity >= 0) {
            designations.put(designation, closestEntity);
        }
    }

    @When("player {string} is ordered to interact with {string}")
    public void orderPlayerToInteract(String playerName, String designation) {
        world.getComponentMapper(OrderQueue.class).create(designations.get(playerName)).requests.add(
                new InteractOrderRequest(designations.get(designation)));
    }

    @When("player {string} is ordered to move {float}, {float} from current position")
    public void orderPlayerToMove(String playerName, float dx, float dy) {
        Point2f avatarPosition = world.getComponentMapper(Transform.class).get(designations.get(playerName)).position;
        orderPlayerToMoveTo(playerName, avatarPosition.x + dx, avatarPosition.y + dy);
    }

    @When("player {string} is ordered to move to {float}, {float}")
    public void orderPlayerToMoveTo(String playerName, float x, float y) {
        int avatarEntity = designations.get(playerName);
        world.getComponentMapper(OrderQueue.class).create(designations.get(playerName)).requests.add(
                new MoveOrderRequest(new Point2f(x, y)));
    }

    @When("waiting until player {string} has no order")
    public void waitForPlayerToHaveNoOrder(String playerName) {
        await().timeout(Duration.ONE_MINUTE).until(() ->
                !world.getComponentMapper(OrderQueue.class).has(designations.get(playerName)) &&
                !world.getComponentMapper(Order.class).has(designations.get(playerName)));
    }

    @When("waiting {float} seconds")
    public void wait(float seconds) {
        int timeEntity = world.getEntities(Aspect.all(Time.class))[0];
        Time time = world.getComponentMapper(Time.class).get(timeEntity);
        float startTime = time.time;
        await().timeout(Duration.FOREVER).until(() -> time.time >= startTime + seconds);
    }

    @Then("{string} should exist")
    public void shouldExist(String designation) {
        Assert.assertTrue(world.doesEntityExist(designations.get(designation)));
    }

    @Then("{string} should not exist")
    public void shouldNotExist(String designation) {
        Assert.assertFalse(world.doesEntityExist(designations.get(designation)));
    }

    @Then("player {string} should have reached position {float}, {float}")
    public void shouldHaveReachedPosition(String playerName, float x, float y) {
        Point2f playerPosition = world.getComponentMapper(Transform.class).get(designations.get(playerName)).position;
        Point2f targetPosition = new Point2f(x, y);
        Vector2f diff = new Vector2f(playerPosition.x - targetPosition.x, playerPosition.y - targetPosition.y);
        float distance = diff.length();
        Assert.assertTrue(distance < 0.1f);
    }

    @Then("player {string} should not have reached position {float}, {float}")
    public void shouldNotHaveReachedPosition(String playerName, float x, float y) {
        Point2f playerPosition = world.getComponentMapper(Transform.class).get(designations.get(playerName)).position;
        Point2f targetPosition = new Point2f(x, y);
        Vector2f diff = new Vector2f(playerPosition.x - targetPosition.x, playerPosition.y - targetPosition.y);
        float distance = diff.length();
        Assert.assertTrue(distance > 0.2f);
    }

    @Then("player {string} should not be close to position {float}, {float}")
    public void playerShouldNotBeCloseToPosition(String playerName, float x, float y) {
        Point2f playerPosition = world.getComponentMapper(Transform.class).get(designations.get(playerName)).position;
        Point2f targetPosition = new Point2f(x, y);
        Vector2f diff = new Vector2f(playerPosition.x - targetPosition.x, playerPosition.y - targetPosition.y);
        float distance = diff.length();
        Assert.assertTrue(distance > 2f);
    }

    @Then("player {string} should have at least {int} of item {string}")
    public void playerShouldHaveItem(String playerName, int amount, String itemName) {
        Inventory playerInventory = world.getComponentMapper(Inventory.class).get(designations.get(playerName));
        Assert.assertTrue(playerInventory.has(world.getItems().getItem(itemName).getId(), amount));
    }

    @Then("{string} should not be in use")
    public void shouldNotBeInUse(String designation) {
        Assert.assertFalse(world.getComponentMapper(Used.class).has(designations.get(designation)));
    }
}