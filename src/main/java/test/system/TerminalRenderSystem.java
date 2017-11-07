package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalSystem;
import com.artemis.utils.IntBag;
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.terminal.Terminal;
import test.component.*;

import java.nio.charset.Charset;

public class TerminalRenderSystem extends IntervalSystem {
	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<ActiveComponent> activeComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;

	private Terminal terminal;

	public TerminalRenderSystem() {
		super(Aspect.all(), 0.2f);
	}

	@Override
	protected void initialize() {
		terminal = TerminalFacade.createTerminal(System.in, System.out, Charset.forName("UTF8"));
		terminal.setCursorVisible(false);
		terminal.enterPrivateMode();
	}

	@Override
	protected void processSystem() {
		terminal.clearScreen();
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.one(TransformComponent.class)).getEntities();
		terminal.moveCursor(0, 0);
		terminal.applyForegroundColor(Terminal.Color.WHITE);
		String s = "Entities: " + entities.size();
		for (int i = 0; i < s.length(); i++){
			terminal.putCharacter(s.charAt(i));
		}
		for (int i = 0; i < entities.size(); i++) {
			int entity = entities.get(i);
			TransformComponent transformComponent = transformComponentMapper.get(entity);
			int column = terminal.getTerminalSize().getColumns() / 2 + (int) (transformComponent.position.x * 3);
			int row = terminal.getTerminalSize().getRows() / 2 + (int) (transformComponent.position.y * 3);
			if (column >= 0 && column < terminal.getTerminalSize().getColumns() &&
					row >= 0 && row < terminal.getTerminalSize().getRows()) {
				terminal.moveCursor(column, row);
				if (peerComponentMapper.has(entity)) {
					if (activeComponentMapper.has(entity)) {
						terminal.applyForegroundColor(Terminal.Color.BLUE);
					} else {
						terminal.applyForegroundColor(Terminal.Color.YELLOW);
					}
					terminal.putCharacter('O');
				} else {
					if (typeComponentMapper.get(entity).type.equals("ai")) {
						terminal.applyForegroundColor(Terminal.Color.CYAN);
						terminal.putCharacter('o');
					} else if (typeComponentMapper.get(entity).type.equals("tree")) {
						terminal.applyForegroundColor(Terminal.Color.GREEN);
						terminal.putCharacter('+');
					} else {
						terminal.applyForegroundColor(Terminal.Color.MAGENTA);
						terminal.putCharacter('x');
					}
				}
				if (pathComponentMapper.has(entity)) {
					PathComponent pathComponent = pathComponentMapper.get(entity);
					int targetColumn = terminal.getTerminalSize().getColumns() / 2 + (int) (pathComponent.targetPosition.x * 3);
					int targetRow = terminal.getTerminalSize().getRows() / 2 + (int) (pathComponent.targetPosition.y * 3);
					terminal.moveCursor(targetColumn, targetRow);
					terminal.applyForegroundColor(Terminal.Color.RED);
					terminal.putCharacter('x');
				}
			}
		}
		terminal.flush();
	}
}
