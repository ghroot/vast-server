package com.vast.system;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalSystem;
import com.artemis.utils.Bag;
import com.artemis.utils.IntBag;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.vast.Metrics;
import com.vast.Properties;
import com.vast.VastPeer;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.*;
import java.util.stream.Collectors;

public class TerminalSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(TerminalSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Constructable> constructableMapper;

	private Map<String, VastPeer> peers;
	private Metrics metrics;
	private WorldConfiguration worldConfiguration;
	private Map<Integer, Set<Integer>> spatialHashes;

	private Screen screen;
	private float scale = 3.0f;
	private Point2f cameraPosition = new Point2f();
	private boolean showPlayerNames = false;
	private boolean showIds = false;
	private int showSystemTimesMode = 0;
	private boolean showSyncedProperties = false;
	private Map<Integer, String> propertyNames = new HashMap<Integer, String>();
	private int focusedEntity = -1;
	private int processDuration = 0;

	public TerminalSystem(Map<String, VastPeer> peers, Metrics metrics, WorldConfiguration worldConfiguration, Map<Integer, Set<Integer>> spatialHashes) {
		super(Aspect.all(), 0.1f);
		this.peers = peers;
		this.metrics = metrics;
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;

		propertyNames.put(Properties.POSITION, "Position");
		propertyNames.put(Properties.ROTATION, "Rotation");
		propertyNames.put(Properties.ACTIVE, "Active");
		propertyNames.put(Properties.DURABILITY, "Durability");
		propertyNames.put(Properties.HEALTH, "Health");
		propertyNames.put(Properties.MAX_HEALTH, "MaxHealth");
		propertyNames.put(Properties.PROGRESS, "Progress");
		propertyNames.put(Properties.INVENTORY, "Inventory");
		propertyNames.put(Properties.FUELED, "Fueled");
	}

	@Override
	protected void initialize() {
		try {
			Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
			screen = new TerminalScreen(terminal);
			screen.setCursorPosition(null);
			screen.startScreen();
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void dispose() {
		try {
			screen.stopScreen();
		} catch (Exception ignored) {
		}
	}

	@Override
	protected void processSystem() {
		long startTime = System.currentTimeMillis();
		try {
			handleInput();

			screen.doResizeIfNecessary();
			screen.clear();

			IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(Transform.class)).getEntities();
			IntBag staticEntities = world.getAspectSubscriptionManager().get(Aspect.all(Static.class)).getEntities();
			IntBag playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
			IntBag activePlayerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class)).getEntities();
			IntBag scanEntities = world.getAspectSubscriptionManager().get(Aspect.all(Scan.class)).getEntities();

			if (focusedEntity >= 0 && (world.getEntity(focusedEntity) == null || !transformMapper.has(focusedEntity))) {
				focusedEntity = -1;
			}

			int numberOfEntitiesOnScreen = 0;
			for (int i = 0; i < entities.size(); i++) {
				int entity = entities.get(i);
				Transform transform = transformMapper.get(entity);

				boolean colored = true;
				if (focusedEntity >= 0 && scanMapper.has(focusedEntity)) {
					colored = scanMapper.get(focusedEntity).nearbyEntities.contains(entity);
				}
				TextColor gray = TextColor.ANSI.Indexed.fromRGB(50, 50, 50);

				TerminalPosition terminalPosition = getTerminalPositionFromWorldPosition(transform.position);
				if (terminalPosition.getColumn() >= 0 && terminalPosition.getColumn() < screen.getTerminalSize().getColumns() &&
						terminalPosition.getRow() >= 0 && terminalPosition.getRow() < screen.getTerminalSize().getRows()) {
					if (playerMapper.has(entity)) {
						TextGraphics textGraphics = screen.newTextGraphics();
						if (activeMapper.has(entity)) {
							screen.setCharacter(terminalPosition, new TextCharacter('O', colored ? TextColor.ANSI.BLUE : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('O', colored ? TextColor.ANSI.YELLOW : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.YELLOW);
						}
						if (showPlayerNames) {
							textGraphics.putString(terminalPosition.getColumn() + 2, terminalPosition.getRow(), playerMapper.get(entity).name);
						} else if (showIds) {
							textGraphics.putString(terminalPosition.getColumn() + 2, terminalPosition.getRow(), "" + entity);
						}
					} else if (typeMapper.has(entity)) {
						TextGraphics textGraphics = screen.newTextGraphics();
						if (typeMapper.get(entity).type.equals("ai")) {
							screen.setCharacter(terminalPosition, new TextCharacter('o', colored ? TextColor.ANSI.CYAN : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.CYAN);
						} else if (typeMapper.get(entity).type.equals("tree")) {
							screen.setCharacter(terminalPosition, new TextCharacter('+', colored ? TextColor.ANSI.GREEN : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
						} else if (typeMapper.get(entity).type.equals("rock")) {
							screen.setCharacter(terminalPosition, new TextCharacter('^', colored ? TextColor.ANSI.Indexed.fromRGB(100, 100, 100) : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
						} else if (typeMapper.get(entity).type.equals("crate")) {
							screen.setCharacter(terminalPosition, new TextCharacter('.', colored ? TextColor.ANSI.RED : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.RED);
						} else if (typeMapper.get(entity).type.equals("building")) {
							Constructable constructable = constructableMapper.get(entity);
							if (constructable.isComplete()) {
								screen.setCharacter(terminalPosition, new TextCharacter('#', colored ? TextColor.ANSI.WHITE : gray, TextColor.ANSI.DEFAULT));
							} else {
								screen.setCharacter(terminalPosition, new TextCharacter('#', colored ? TextColor.ANSI.Indexed.fromRGB(100, 100, 100) : gray, TextColor.ANSI.DEFAULT));
							}
							textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
						} else if (typeMapper.get(entity).type.equals("home")) {
							screen.setCharacter(terminalPosition, new TextCharacter('X', colored ? TextColor.ANSI.RED : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.RED);
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('?', colored ? TextColor.ANSI.MAGENTA : gray, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.MAGENTA);
						}
						if (showIds) {
							textGraphics.putString(terminalPosition.getColumn() + 2, terminalPosition.getRow(), "" + entity);
						}
					}
					numberOfEntitiesOnScreen++;
				}
			}

			TextGraphics textGraphics = screen.newTextGraphics();

			textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
			textGraphics.putString(0, 0, "World size: " + worldConfiguration.width + " x " + worldConfiguration.height);
			textGraphics.putString(0, 1, "Scale: x" + (Math.round(scale * 100.0f) / 100.0f));
			textGraphics.putString(0, 2, "Camera position: " + (Math.round(cameraPosition.x * 100.0f) / 100.0f) + ", " + (Math.round(cameraPosition.y * 100.0f) / 100.0f));
			textGraphics.putString(0, 3, "Spatial hash size: " + worldConfiguration.sectionSize);
			int numberOfSpatialHashes = 0;
			int numberOfActiveSpatialHashes = 0;
			for (Set<Integer> entitiesInSpatialHash : spatialHashes.values()) {
				numberOfSpatialHashes++;
				if (entitiesInSpatialHash.size() > 0) {
					numberOfActiveSpatialHashes++;
				}
			}
			textGraphics.putString(0, 4, "Active spatial hashes: " + numberOfActiveSpatialHashes + " (of " + numberOfSpatialHashes + " total)");
			textGraphics.putString(0, 5, "Total entities: " + entities.size() + " (" + numberOfEntitiesOnScreen + " on screen)");
			textGraphics.putString(0, 6, "Moving / static entities: " + (entities.size() - staticEntities.size()) + " / " + staticEntities.size());
			textGraphics.putString(0, 7, "Player entities: " + playerEntities.size() + " (" + activePlayerEntities.size() + " active)");
			textGraphics.putString(0, 8, "Scanning entities: " + scanEntities.size());

			String fpsString = "FPS: " + metrics.getFps();
			textGraphics.putString(screen.getTerminalSize().getColumns() - fpsString.length(), 0, fpsString);
			String frameTimeString = "Frame time: " + metrics.getTimePerFrameMs() + " ms";
			textGraphics.putString(screen.getTerminalSize().getColumns() - frameTimeString.length(), 1, frameTimeString);
			String monitorProcessDurationString = "Monitor overhead: " + (int) (processDuration / 5.8f) + " ms";
			textGraphics.putString(screen.getTerminalSize().getColumns() - monitorProcessDurationString.length(), 2, monitorProcessDurationString);
			String peersString = "Peers: " + peers.size();
			textGraphics.putString(screen.getTerminalSize().getColumns() - peersString.length(), 3, peersString);
			String roundTripString = "Roundtrip: " + (int) metrics.getMeanOfRoundTripTime();
			textGraphics.putString(screen.getTerminalSize().getColumns() - roundTripString.length(), 4, roundTripString);
			String sendMessagesString = "Sent messages: " + metrics.getNumberOfSentMessages();
			textGraphics.putString(screen.getTerminalSize().getColumns() - sendMessagesString.length(), 5, sendMessagesString);
			String timeSinceSave = "Time since save: " + (metrics.getTimeSinceLastSerialization() / 1000) + " s";
			textGraphics.putString(screen.getTerminalSize().getColumns() - timeSinceSave.length(), 6, timeSinceSave);
			String collisionsString = "Collision checks: " + metrics.getNumberOfCollisionChecks();
			textGraphics.putString(screen.getTerminalSize().getColumns() - collisionsString.length(), 7, collisionsString);

			if (showSystemTimesMode > 0 && metrics.getSystemProcessingTimes().size() > 0) {
				int longestLength = 0;
				for (String systemName : metrics.getSystemProcessingTimes().keySet()) {
					longestLength = Math.max(systemName.length(), longestLength);
				}
				Map<String, Integer> systemProcessingTimesToShow;
				if (showSystemTimesMode == 1) {
					systemProcessingTimesToShow = metrics.getSystemProcessingTimes().entrySet()
							.stream()
							.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
							.collect(Collectors.toMap(
									Map.Entry::getKey,
									Map.Entry::getValue,
									(e1, e2) -> e1,
									LinkedHashMap::new
							));
				} else {
					systemProcessingTimesToShow = metrics.getSystemProcessingTimes().entrySet()
							.stream()
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(
									Map.Entry::getKey,
									Map.Entry::getValue,
									(e1, e2) -> e1,
									LinkedHashMap::new
							));
				}
				int row = 9;
				for (String systemName : systemProcessingTimesToShow.keySet()) {
					int processingDuration = systemProcessingTimesToShow.get(systemName);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 6 - longestLength - 1, row, systemName);
					String processDurationString = Integer.toString(processingDuration);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 6 + (3 - processDurationString.length()), row, processDurationString);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 2, row, "ms");
					row++;
				}
			}

			if (showSyncedProperties) {
				int longestLength = 0;
				for (int property : metrics.getSyncedProperties().keySet()) {
					String propertyName = propertyNames.get(property);
					longestLength = Math.max(propertyName.length(), longestLength);
				}
				int row = 11;
				for (int property : metrics.getSyncedProperties().keySet()) {
					int count = metrics.getSyncedProperties().get(property);
					String propertyName = propertyNames.get(property);
					textGraphics.putString(0, row, propertyName);
					textGraphics.putString(longestLength + 1, row, "" + count);
					row++;
				}
			}

			if (focusedEntity >= 0) {
				Point2f position = transformMapper.get(focusedEntity).position;
				cameraPosition.set(position.x, -position.y);
				if (playerMapper.has(focusedEntity)) {
					textGraphics.putString(0, 9, "Following entity: " + focusedEntity + " (" + playerMapper.get(focusedEntity).name + ")");
				} else {
					textGraphics.putString(0, 9, "Following entity: " + focusedEntity);
				}

				Bag<Component> components = new Bag<Component>();
				world.getEntity(focusedEntity).getComponents(components);
				int row = screen.getTerminalSize().getRows() - components.size();
				for (int i = 0; i < components.size(); i++) {
					Component component = components.get(i);
					String componentName = component.getClass().toString();
					componentName = componentName.substring(componentName.lastIndexOf(".") + 1);
					String detail = null;
					if (component instanceof Type) {
						detail = ((Type) component).type;
					} else if (component instanceof SubType) {
						detail = "" + ((SubType) component).subType;
					} else if (component instanceof Interact) {
						detail = "" + ((Interact) component).entity;
					} else if (component instanceof Scan) {
						detail = "" + ((Scan) component).nearbyEntities.size();
					} else if (component instanceof Known) {
						detail = "" + ((Known) component).knownEntities.size();
					} else if (component instanceof AI) {
						detail = ((AI) component).behaviourName;
					} else if (component instanceof Health) {
						detail = "" + ((Health) component).health;
					} else if (component instanceof Collision) {
						detail = "" + (Math.round(((Collision) component).radius * 100.0f) / 100.0f);
					} else if (component instanceof Owner) {
						detail = ((Owner) component).name;
					} else if (component instanceof Player) {
						detail = ((Player) component).name;
					} else if (component instanceof Home) {
						detail = ((Home) component).name;
					} else if (component instanceof Spatial) {
						detail = "" + ((Spatial) component).memberOfSpatialHash.x + ", " + ((Spatial) component).memberOfSpatialHash.y;
					} else if (component instanceof Transform) {
						detail = "" + (Math.round(((Transform) component).position.x * 100.0f) / 100.0f) + ", " + (Math.round(((Transform) component).position.y * 100.0f) / 100.0f);
					} else if (component instanceof Path) {
						detail = "" + (Math.round(((Path) component).targetPosition.x * 100.0f) / 100.0f) + ", " + (Math.round(((Path) component).targetPosition.y * 100.0f) / 100.0f);
					} else if (component instanceof Inventory) {
						Inventory inventory = (Inventory) component;
						StringBuilder s = new StringBuilder();
						for (int j = 0; j < inventory.items.length; j++) {
							s.append(inventory.items[j]);
							if (j < inventory.items.length - 1) {
								s.append(", ");
							}
						}
						detail = s.toString();
					} else if (component instanceof Lifetime) {
						detail = "" + (Math.round(((Lifetime) component).timeLeft * 100.0f) / 100.0f);
					}
					if (detail != null) {
						textGraphics.putString(0, row, componentName + " (" + detail + ")");
					} else {
						textGraphics.putString(0, row, componentName);
					}
					row++;
				}
			}

			screen.refresh();
		} catch (Exception exception) {
			logger.error("Error displaying metrics", exception);
		}
		long endTime = System.currentTimeMillis();
		processDuration = (int) (endTime - startTime);
	}

	private void handleInput() {
		try {
			KeyStroke keyStroke = screen.pollInput();
			if (keyStroke != null) {
				if (keyStroke.getKeyType() == KeyType.Character) {
					if (keyStroke.getCharacter().toString().equals("+")) {
						if (scale <= 0.04f) {
							scale += 0.01f;
						} else {
							scale += 0.05f;
						}
					} else if (keyStroke.getCharacter().toString().equals("?")) {
						scale += 1.0f;
					} else if (keyStroke.getCharacter().toString().equals("-")) {
						if (scale <= 0.05f) {
							scale -= 0.01f;
						} else {
							scale -= 0.05f;
						}
						scale = Math.max(scale, 0.01f);
					} else if (keyStroke.getCharacter().toString().equals("_")) {
						scale -= 1.0f;
						scale = Math.max(scale, 0.01f);
					} else if (keyStroke.getCharacter().toString().equals("p") || keyStroke.getCharacter().toString().equals("a")) {
						IntBag playerEntities;
						if (keyStroke.getCharacter().toString().equals("p")) {
							playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class).exclude(AI.class)).getEntities();
						} else {
							playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class).exclude(AI.class)).getEntities();
						}
						if (playerEntities.size() > 0) {
							int playerEntity;
							if (focusedEntity == -1) {
								playerEntity = playerEntities.get(0);
							} else {
								int index = playerEntities.indexOf(focusedEntity);
								if (index >= 0) {
									int nextIndex = index + 1;
									if (nextIndex < playerEntities.size()) {
										playerEntity = playerEntities.get(nextIndex);
									} else {
										playerEntity = playerEntities.get(0);
									}
								} else {
									playerEntity = playerEntities.get(0);
								}
							}
							if (playerMapper.has(playerEntity) && transformMapper.has(playerEntity)) {
								Point2f position = transformMapper.get(playerEntity).position;
								cameraPosition.set(position.x, -position.y);
								focusedEntity = playerEntity;
							}
						}
					} else if (keyStroke.getCharacter().toString().equals("f")) {
						IntBag transformEntities = world.getAspectSubscriptionManager().get(Aspect.all(Transform.class)).getEntities();
						int closestEntity = -1;
						float closestDistance = Float.MAX_VALUE;
						Point2f cameraWorldPosition = new Point2f(cameraPosition.x, -cameraPosition.y);
						for (int i = 0; i < transformEntities.size(); i++) {
							int entity = transformEntities.get(i);
							float distance = cameraWorldPosition.distance(transformMapper.get(entity).position);
							if (distance < closestDistance) {
								closestEntity = entity;
								closestDistance = distance;
							}
						}
						if (closestEntity >= 0) {
							focusedEntity = closestEntity;
						}
					} else if (keyStroke.getCharacter().toString().equals("r")) {
						cameraPosition.set(0.0f, 0.0f);
						focusedEntity = -1;
					} else if (keyStroke.getCharacter().toString().equals("n")) {
						showPlayerNames = !showPlayerNames;
						if (showPlayerNames) {
							showIds = false;
						}
					} else if (keyStroke.getCharacter().toString().equals("i")) {
						showIds = !showIds;
						if (showIds) {
							showPlayerNames = false;
						}
					} else if (keyStroke.getCharacter().toString().equals("s")) {
						if (showSystemTimesMode >= 2) {
							showSystemTimesMode = 0;
						} else {
							showSystemTimesMode++;
						}
					} else if (keyStroke.getCharacter().toString().equals("y")) {
						showSyncedProperties = !showSyncedProperties;
					}
				} else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
					cameraPosition.add(new Point2f(0.0f, ((keyStroke.isShiftDown() ? 5.0f : 1.0f) / scale)));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
					cameraPosition.add(new Point2f(0.0f, -((keyStroke.isShiftDown() ? 5.0f : 1.0f) / scale)));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
					cameraPosition.add(new Point2f(-((keyStroke.isShiftDown() ? 5.0f : 1.0f) / scale), 0.0f));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
					cameraPosition.add(new Point2f(((keyStroke.isShiftDown() ? 5.0f : 1.0f) / scale), 0.0f));
					focusedEntity = -1;
				}
			}
		} catch (Exception ignored) {
		}
	}

	private TerminalPosition getTerminalPositionFromWorldPosition(Point2f position) {
		return new TerminalPosition(
				screen.getTerminalSize().getColumns() / 2 + (int) (position.x * scale) - (int) (cameraPosition.x * scale),
				screen.getTerminalSize().getRows() / 2 - (int) (position.y * scale) - (int) (cameraPosition.y * scale)
		);
	}
}
