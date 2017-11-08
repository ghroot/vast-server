package test.system;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Metrics;
import test.Profiler;
import test.WorldDimensions;
import test.component.*;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class TerminalSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(TerminalSystem.class);

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;

	private Metrics metrics;
	private WorldDimensions worldDimensions;
	Map<Point2i, Set<Integer>> spatialHashes;

	private Screen screen;
	private Point2i offset = new Point2i();
	private float scale = 3.0f;
	private boolean showPathTargetPosition = false;
	private boolean showSystemProcessingDurations = false;

	public TerminalSystem(Metrics fps, WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(), 0.1f);
		this.metrics = fps;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
	}

	@Override
	protected void initialize() {
		try {
			Terminal terminal = new DefaultTerminalFactory().createTerminalEmulator();
			screen = new TerminalScreen(terminal);
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

			IntBag entities = world.getAspectSubscriptionManager().get(Aspect.one(TransformComponent.class)).getEntities();
			IntBag peerEntities = world.getAspectSubscriptionManager().get(Aspect.one(PeerComponent.class)).getEntities();
			IntBag activePeerEntities = world.getAspectSubscriptionManager().get(Aspect.all(PeerComponent.class, ActiveComponent.class)).getEntities();

			if (showPathTargetPosition) {
				IntBag pathEntities = world.getAspectSubscriptionManager().get(Aspect.all(TransformComponent.class, PathComponent.class)).getEntities();
				for (int i = 0; i < pathEntities.size(); i++) {
					int pathEntity = pathEntities.get(i);

					PathComponent pathComponent = pathComponentMapper.get(pathEntity);
					TerminalPosition targetTerminalPosition = getTerminalPositionFromWorldPosition(pathComponent.targetPosition);
					if (targetTerminalPosition.getColumn() >= 0 && targetTerminalPosition.getColumn() < screen.getTerminalSize().getColumns() &&
							targetTerminalPosition.getRow() >= 0 && targetTerminalPosition.getRow() < screen.getTerminalSize().getRows()) {
						screen.setCharacter(targetTerminalPosition, new TextCharacter('x', TextColor.ANSI.RED, TextColor.ANSI.DEFAULT));
					}
				}
			}

			int numberOfEntitiesOnScreen = 0;
			for (int i = 0; i < entities.size(); i++) {
				int entity = entities.get(i);
				TransformComponent transformComponent = transformComponentMapper.get(entity);
				TerminalPosition terminalPosition = getTerminalPositionFromWorldPosition(transformComponent.position);
				if (terminalPosition.getColumn() >= 0 && terminalPosition.getColumn() < screen.getTerminalSize().getColumns() &&
						terminalPosition.getRow() >= 0 && terminalPosition.getRow() < screen.getTerminalSize().getRows()) {
					if (peerComponentMapper.has(entity)) {
						if (activeComponentMapper.has(entity)) {
							screen.setCharacter(terminalPosition, new TextCharacter('O', TextColor.ANSI.BLUE, TextColor.ANSI.DEFAULT));
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('O', TextColor.ANSI.YELLOW, TextColor.ANSI.DEFAULT));
						}
					} else {
						if (typeComponentMapper.get(entity).type.equals("ai")) {
							screen.setCharacter(terminalPosition, new TextCharacter('o', TextColor.ANSI.CYAN, TextColor.ANSI.DEFAULT));
						} else if (typeComponentMapper.get(entity).type.equals("tree")) {
							screen.setCharacter(terminalPosition, new TextCharacter('+', TextColor.ANSI.GREEN, TextColor.ANSI.DEFAULT));
						} else {
							screen.setCharacter(terminalPosition, new TextCharacter('?', TextColor.ANSI.MAGENTA, TextColor.ANSI.DEFAULT));
						}
					}
					numberOfEntitiesOnScreen++;
				}
			}

			TextGraphics textGraphics = screen.newTextGraphics();

			textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
			textGraphics.putString(0, 0, "World size: " + worldDimensions.width + " x " + worldDimensions.height);
			textGraphics.putString(0, 1, "Scale: x" + (Math.round(scale * 100.0) / 100.0));
			textGraphics.putString(0, 2, "Spatial hash size: " + worldDimensions.sectionSize);
			int numberOfSpatialHashes = 0;
			int numberOfActiveSpatialHashes = 0;
			for (Set<Integer> entitiesInSpatialHash : spatialHashes.values()) {
				numberOfSpatialHashes++;
				if (entitiesInSpatialHash.size() > 0) {
					numberOfActiveSpatialHashes++;
				}
			}
			textGraphics.putString(0, 3, "Active spatial hashes: " + numberOfActiveSpatialHashes + " (of " + numberOfSpatialHashes + " total)");
			textGraphics.putString(0, 4, "Total entities: " + entities.size() + " (" + numberOfEntitiesOnScreen + " on screen)");
			textGraphics.putString(0, 5, "Peer entities: " + peerEntities.size() + " (" + activePeerEntities.size() + " active)");
			textGraphics.putString(0, 6, "Showing path targets: " + (showPathTargetPosition ? "Yes" : "No"));

			textGraphics.putString(screen.getTerminalSize().getColumns() - 8, 0, "FPS: " + metrics.fps);
			textGraphics.putString(screen.getTerminalSize().getColumns() - 17, 1, "Frame time: " + metrics.timePerFrameMs + "ms");

			if (showSystemProcessingDurations) {
				int row = 4;
				for (String systemName : metrics.systemProcessingTimes.keySet()) {
					int processingDuration = metrics.systemProcessingTimes.get(systemName);
					textGraphics.putString(screen.getTerminalSize().getColumns() - 50, row, systemName + " " + processingDuration + "ms");
					row++;
				}
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
						scale += 0.05f;
					} else if (keyStroke.getCharacter().toString().equals("?")) {
						scale += 1.0f;
					} else if (keyStroke.getCharacter().toString().equals("-")) {
						scale -= 0.05f;
						scale = Math.max(scale, 0.05f);
					} else if (keyStroke.getCharacter().toString().equals("_")) {
						scale -= 1.0f;
						scale = Math.max(scale, 0.05f);
					} else if (keyStroke.getCharacter().toString().equals("x")) {
						showPathTargetPosition = !showPathTargetPosition;
					} else if (keyStroke.getCharacter().toString().equals("s")) {
						showSystemProcessingDurations = !showSystemProcessingDurations;
					} else if (keyStroke.getCharacter().toString().equals("p") || keyStroke.getCharacter().toString().equals("a")) {
						IntBag peerEntities = world.getAspectSubscriptionManager().get(Aspect.all(PeerComponent.class)).getEntities();
						if (peerEntities.size() > 0) {
							int peerEntity = peerEntities.get(0);
							Point2f position = transformComponentMapper.get(peerEntity).position;
							offset.setX(-(int) (position.x * scale));
							offset.setY((int) (position.y * scale));
						}
					}
				} else if (keyStroke.getKeyType() == KeyType.ArrowDown) {
					offset.add(new Point2i(0, keyStroke.isShiftDown() ? -4 : -1));
				} else if (keyStroke.getKeyType() == KeyType.ArrowUp) {
					offset.add(new Point2i(0, keyStroke.isShiftDown() ? 4 : 1));
				} else if (keyStroke.getKeyType() == KeyType.ArrowLeft) {
					offset.add(new Point2i(keyStroke.isShiftDown() ? 4 : 1, 0));
				} else if (keyStroke.getKeyType() == KeyType.ArrowRight) {
					offset.add(new Point2i(keyStroke.isShiftDown() ? -4 : -1, 0));
				}
			}
		} catch (Exception ignored) {
		}
	}

	private TerminalPosition getTerminalPositionFromWorldPosition(Point2f position) {
		return new TerminalPosition(
				screen.getTerminalSize().getColumns() / 2 + (int) (position.x * scale) + offset.x,
				screen.getTerminalSize().getRows() / 2 - (int) (position.y * scale) + offset.y
		);
	}
}
