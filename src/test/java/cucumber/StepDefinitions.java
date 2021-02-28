package cucumber;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.RequestMessage;
import com.vast.VastWorld;
import com.vast.component.*;
import com.vast.data.*;
import com.vast.network.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Duration;

import javax.vecmath.Point2f;
import java.util.*;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StepDefinitions {
    private List<IncomingRequest> incomingRequests;
    private List<PeerListener> peerListeners;
    private VastWorld world;
    private Thread worldThread;

    private Map<String, Integer> designations;

    private VastServerApplication createServerApplication() {
        VastServerApplication serverApplication = mock(VastServerApplication.class);
        when(serverApplication.getIncomingRequests()).thenReturn(incomingRequests);
        doAnswer(invocation -> {
            peerListeners.add(invocation.getArgument(0));
            return null;
        }).when(serverApplication).addPeerListener(any(PeerListener.class));

        return serverApplication;
    }

    @Before
    public void setUp() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        designations = new HashMap<>();
    }

    @After
    public void tearDown(Scenario scenario){
        world.destroy();
    }

    private void sendInteractRequest(VastPeer peer, int entityId) {
        DataObject dataObject = new DataObject();
        dataObject.set(MessageCodes.INTERACT_ENTITY_ID, entityId);
        RequestMessage message = new RequestMessage(MessageCodes.INTERACT, dataObject);
        IncomingRequest incomingRequest = new IncomingRequest(peer, message);
        synchronized (incomingRequests) {
            incomingRequests.add(incomingRequest);
        }
    }

    private void sendMoveRequest(VastPeer peer, float x, float y) {
        DataObject dataObject = new DataObject();
        dataObject.set(MessageCodes.MOVE_POSITION, new float[] {x, y});
        RequestMessage message = new RequestMessage(MessageCodes.MOVE, dataObject);
        IncomingRequest incomingRequest = new IncomingRequest(peer, message);
        synchronized (incomingRequests) {
            incomingRequests.add(incomingRequest);
        }
    }

    private void sendFollowRequest(VastPeer peer, int entityId) {
        DataObject dataObject = new DataObject();
        dataObject.set(MessageCodes.FOLLOW_ENTITY_ID, entityId);
        RequestMessage message = new RequestMessage(MessageCodes.FOLLOW, dataObject);
        IncomingRequest incomingRequest = new IncomingRequest(peer, message);
        synchronized (incomingRequests) {
            incomingRequests.add(incomingRequest);
        }
    }

    @Given("a world with size {int} x {int} and seed {long}")
    public void givenWorldWithSize(int width, int height, long seed) {
        incomingRequests = new ArrayList<>();
        peerListeners = new ArrayList<>();

        VastServerApplication serverApplication = createServerApplication();

        WorldConfiguration worldConfiguration = new WorldConfiguration(width, height);
        Items items = new Items("items.json");
        Buildings buildings = new Buildings("buildings.json", items);

        world = new VastWorld(serverApplication, null, new Random(seed), true, null,
                worldConfiguration, items, buildings);

        worldThread = new Thread(world, "World");
        worldThread.setPriority(Thread.MAX_PRIORITY);
        worldThread.start();
    }

    @When("player {string} connects")
    public void playerConnects(String playerName) {
        VastPeer peer = mock(VastPeer.class);
        when(peer.getName()).thenReturn(playerName);
        for (PeerListener peerListener : peerListeners) {
            peerListener.peerAdded(peer);
        }

        await().until(() -> world.getPeer(playerName) != null);
        await().until(() -> world.getPeerEntity(playerName) >= 0);
    }

    @When("player {string} has {int} of item {string}")
    public void hasItem(String playerName, int amount, String itemName) {
        world.getComponentMapper(Inventory.class).get(world.getPeerEntity(playerName)).add(
                world.getItems().getItem(itemName).getId(), amount);
    }

    @When("designates the object of type {string} closest to player {string} as {string}")
    public void designate(String type, String playerName, String designation) {
        int closestEntity = -1;
        float closestDistance2 = Float.MAX_VALUE;
        ComponentMapper<Type> typeMapper = world.getComponentMapper(Type.class);
        ComponentMapper<Transform> transformMapper = world.getComponentMapper(Transform.class);
        int playerEntity = world.getPeerEntity(playerName);
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

    @When("order player {string} to interact with {string}")
    public void orderPlayerToInteract(String playerName, String designation) {
        sendInteractRequest(world.getPeer(playerName), designations.get(designation));
    }

    @When("order player {string} to move {float}, {float} from current position")
    public void orderPlayerToMove(String playerName, float dx, float dy) {
        ComponentMapper<Transform> transformMapper = world.getComponentMapper(Transform.class);
        Point2f playerPosition = transformMapper.get(world.getPeerEntity(playerName)).position;
        sendMoveRequest(world.getPeer(playerName), playerPosition.x + dx, playerPosition.y + dy);
    }

    @When("player {string} stops moving")
    public void waitForPlayerToStopMoving(String playerName) {
        await().timeout(Duration.FIVE_MINUTES).until(() ->
                !world.getComponentMapper(Path.class).has(world.getPeerEntity(playerName)));
    }

    @When("order player {string} to follow {string}")
    public void orderPlayerToFollow(String playerName, String designation) {
        sendFollowRequest(world.getPeer(playerName), designations.get(designation));
    }

    @Then("object {string} should not exist")
    public void shouldNotExist(String designation) {
        await().timeout(Duration.ONE_MINUTE).until(() -> !world.doesEntityExist(designations.get(designation)));
    }

    @When("waiting {float} seconds")
    public void wait(float seconds) {
        int timeEntity = world.getEntities(Aspect.all(Time.class))[0];
        Time time = world.getComponentMapper(Time.class).get(timeEntity);
        float startTime = time.time;
        await().timeout(Duration.FIVE_MINUTES).until(() -> time.time >= startTime + seconds);
    }
}