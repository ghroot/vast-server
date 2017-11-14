package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalSystem;
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
import com.vast.Profiler;
import com.vast.WorldDimensions;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class TerminalSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(TerminalSystem.class);

	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Path> pathMapper;

	private Metrics metrics;
	private WorldDimensions worldDimensions;
	private Map<Integer, Set<Integer>> spatialHashes;
	private Map<String, Set<Integer>> nearbyEntitiesByPeer;

	private Screen screen;
	private float scale = 3.0f;
	private Point2f cameraPosition = new Point2f();
	private boolean showPathTargetPosition = false;
	private boolean showPlayerNames = false;
	private int focusedEntity = -1;

	public TerminalSystem(Metrics metrics, WorldDimensions worldDimensions, Map<Integer, Set<Integer>> spatialHashes, Map<String, Set<Integer>> nearbyEntitiesByPeer) {
		super(Aspect.all(), 0.1f);
		this.metrics = metrics;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
		this.nearbyEntitiesByPeer = nearbyEntitiesByPeer;
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
		try {
			handleInput();

			screen.doResizeIfNecessary();
			screen.clear();

			IntBag entities = world.getAspectSubscriptionManager().get(Aspect.one(Transform.class)).getEntities();
			IntBag playerEntities = world.getAspectSubscriptionManager().get(Aspect.one(Player.class)).getEntities();
			IntBag activePlayerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class)).getEntities();

			if (showPathTargetPosition) {
				IntBag pathEntities = world.getAspectSubscriptionManager().get(Aspect.all(Transform.class, Path.class)).getEntities();
				for (int i = 0; i < pathEntities.size(); i++) {
					int pathEntity = pathEntities.get(i);

					Path path = pathMapper.get(pathEntity);
					TerminalPosition targetTerminalPosition = getTerminalPositionFromWorldPosition(path.targetPosition);
					if (targetTerminalPosition.getColumn() >= 0 && targetTerminalPosition.getColumn() < screen.getTerminalSize().getColumns() &&
							targetTerminalPosition.getRow() >= 0 && targetTerminalPosition.getRow() < screen.getTerminalSize().getRows()) {
						screen.setCharacter(targetTerminalPosition, new TextCharacter('x', TextColor.ANSI.RED, TextColor.ANSI.DEFAULT));
					}
				}
			}

			int numberOfEntitiesOnScreen = 0;
			for (int i = 0; i < entities.size(); i++) {
				int entity = entities.get(i);
				Transform transform = transformMapper.get(entity);

				boolean colored = true;
				if (focusedEntity >= 0 && playerMapper.has(focusedEntity)) {
					String name = playerMapper.get(focusedEntity).name;
					if (nearbyEntitiesByPeer.containsKey(name)) {
						colored = nearbyEntitiesByPeer.get(name).contains(entity);
					}
				}
				TextColor gray = TextColor.ANSI.Indexed.fromRGB(50, 50, 50);

				TerminalPosition terminalPosition = getTerminalPositionFromWorldPosition(transform.position);
				if (terminalPosition.getColumn() >= 0 && terminalPosition.getColumn() < screen.getTerminalSize().getColumns() &&
						terminalPosition.getRow() >= 0 && terminalPosition.getRow() < screen.getTerminalSize().getRows()) {
					if (playerMapper.has(entity)) {
						TextGraphics textGraphics = screen.newTextGraphics();
						if (activeMapper.has(entity)) {
							screen.setCharacter(terminalPosition, new TextCharacter('O', TextColor.ANSI.BLUE, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.BLUE);
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('O', TextColor.ANSI.YELLOW, TextColor.ANSI.DEFAULT));
							textGraphics.setForegroundColor(TextColor.ANSI.YELLOW);
						}
						if (showPlayerNames) {
							textGraphics.putString(terminalPosition.getColumn() + 2, terminalPosition.getRow(), playerMapper.get(entity).name);
						}
					} else {
						if (typeMapper.get(entity).type.equals("ai")) {
							screen.setCharacter(terminalPosition, new TextCharacter('o', colored ? TextColor.ANSI.CYAN : gray, TextColor.ANSI.DEFAULT));
						} else if (typeMapper.get(entity).type.equals("tree")) {
							screen.setCharacter(terminalPosition, new TextCharacter('+', colored ? TextColor.ANSI.GREEN : gray, TextColor.ANSI.DEFAULT));
						} else if (typeMapper.get(entity).type.equals("pickup")) {
							screen.setCharacter(terminalPosition, new TextCharacter('.', colored ? TextColor.ANSI.RED : gray, TextColor.ANSI.DEFAULT));
						} else if (typeMapper.get(entity).type.equals("building")) {
							screen.setCharacter(terminalPosition, new TextCharacter('#', colored ? TextColor.ANSI.WHITE : gray, TextColor.ANSI.DEFAULT));
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('?', colored ? TextColor.ANSI.MAGENTA : gray, TextColor.ANSI.DEFAULT));
						}
					}
					numberOfEntitiesOnScreen++;
				}
			}

			TextGraphics textGraphics = screen.newTextGraphics();

			textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
			textGraphics.putString(0, 0, "World size: " + worldDimensions.width + " x " + worldDimensions.height);
			textGraphics.putString(0, 1, "Scale: x" + (Math.round(scale * 100.0f) / 100.0f));
			textGraphics.putString(0, 2, "Camera position: " + (Math.round(cameraPosition.x * 100.0f) / 100.0f) + ", " + (Math.round(cameraPosition.y * 100.0f) / 100.0f));
			textGraphics.putString(0, 3, "Spatial hash size: " + worldDimensions.sectionSize);
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
			textGraphics.putString(0, 6, "Player entities: " + playerEntities.size() + " (" + activePlayerEntities.size() + " active)");

			textGraphics.putString(screen.getTerminalSize().getColumns() - 7, 0, "FPS: " + metrics.getFps());
			textGraphics.putString(screen.getTerminalSize().getColumns() - 17, 1, "Frame time: " + metrics.getTimePerFrameMs() + " ms");
			String roundTripString = "Roundtrip: " + metrics.getMeanOfRoundTripTime() + " (" + metrics.getMeanOfRoundTripTimeDeviation() + ")";
			textGraphics.putString(screen.getTerminalSize().getColumns() - roundTripString.length(), 2, roundTripString);
			textGraphics.putString(screen.getTerminalSize().getColumns() - 23, 4, "Collision checks: " + metrics.getNumberOfCollisionChecks());

			if (metrics.getSystemProcessingTimes().size() > 0) {
				int longestLength = 0;
				for (String systemName : metrics.getSystemProcessingTimes().keySet()) {
					longestLength = Math.max(systemName.length(), longestLength);
				}
				int row = 6;
				for (String systemName : metrics.getSystemProcessingTimes().keySet()) {
					int processingDuration = metrics.getSystemProcessingTimes().get(systemName);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 6 - longestLength - 1, row, systemName);
					String processDurationString = Integer.toString(processingDuration);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 6 + (3 - processDurationString.length()), row, processDurationString);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 2, row, "ms");
					row++;
				}
			}

			if (focusedEntity >= 0) {
				Point2f position = transformMapper.get(focusedEntity).position;
				cameraPosition.set(position.x, -position.y);
				textGraphics.putString(0, 8, "Following peer entity: " + focusedEntity + " (" + playerMapper.get(focusedEntity).name + ")");
			}

			screen.refresh();
		} catch (Exception ignored) {
		}
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
					} else if (keyStroke.getCharacter().toString().equals("x")) {
						showPathTargetPosition = !showPathTargetPosition;
					} else if (keyStroke.getCharacter().toString().equals("p") || keyStroke.getCharacter().toString().equals("a")) {
						IntBag playerEntities;
						if (keyStroke.getCharacter().toString().equals("p")) {
							playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class)).getEntities();
						} else {
							playerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class)).getEntities();
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
							Point2f position = transformMapper.get(playerEntity).position;
							cameraPosition.set(position.x, -position.y);
							focusedEntity = playerEntity;
						}
					} else if (keyStroke.getCharacter().toString().equals("r")) {
						cameraPosition.set(0.0f, 0.0f);
						focusedEntity = -1;
					} else if (keyStroke.getCharacter().toString().equals("n")) {
						showPlayerNames = !showPlayerNames;
					}
				} else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
					cameraPosition.add(new Point2f(0.0f, (1 / scale)));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
					cameraPosition.add(new Point2f(0.0f, -(1 / scale)));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
					cameraPosition.add(new Point2f(-(1 / scale), 0.0f));
					focusedEntity = -1;
				} else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
					cameraPosition.add(new Point2f((1 / scale), 0.0f));
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
