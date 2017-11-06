package test.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.AIComponent;
import test.component.PathComponent;
import test.component.SyncTransformComponent;
import test.component.TransformComponent;

import javax.vecmath.Point2f;

public class AISystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(AISystem.class);

	private ComponentMapper<AIComponent> aiComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;

	private Archetype aiEntityArchetype;

	public AISystem() {
		super(Aspect.all(AIComponent.class));
	}

	@Override
	protected void initialize() {
		aiEntityArchetype = new ArchetypeBuilder()
				.add(TransformComponent.class)
				.add(SyncTransformComponent.class)
				.add(AIComponent.class)
				.build(world);

		for (int i = 0; i < 10; i++) {
			int entity = world.create(aiEntityArchetype);
			logger.info("Creating AI entity: {}", entity);
		}
	}

	@Override
	protected void process(int entity) {
		AIComponent aiComponent = aiComponentMapper.get(entity);
		if (aiComponent.countdown == 0) {
			aiComponent.countdown = (int) (1000 + Math.random() * 4000);
		} else {
			aiComponent.countdown = Math.max(aiComponent.countdown - 167, 0);
			if (aiComponent.countdown == 0) {
				pathComponentMapper.create(entity).targetPosition = new Point2f((float) (-2.5f + Math.random() * 5.0f), (float) (-2.5f + Math.random() * 5.0f));
			}
		}
	}
}
