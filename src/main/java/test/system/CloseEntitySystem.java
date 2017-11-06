package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalSystem;
import com.artemis.utils.IntBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.TransformComponent;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloseEntitySystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(CloseEntitySystem.class);

	private final float MAX_DISTANCE_SQUARED = 0.2f * 0.2f;

	private ComponentMapper<TransformComponent> transformComponentMapper;

	private Map<Integer, List<Integer>> closeEntitiesByEntity;

	private Vector2f reusableVector;

	public CloseEntitySystem(Map<Integer, List<Integer>> closeEntitiesByEntity) {
		super(Aspect.all(), 1000);
		this.closeEntitiesByEntity = closeEntitiesByEntity;
		reusableVector = new Vector2f();
	}

	@Override
	protected void processSystem() {
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(TransformComponent.class)).getEntities();
		for (int i = 0; i < entities.size(); i++) {
			int entity = entities.get(i);
			TransformComponent transformComponent = transformComponentMapper.get(entity);
			List<Integer> closeEntities;
			if (closeEntitiesByEntity.containsKey(entity)) {
				closeEntities = closeEntitiesByEntity.get(entity);
				closeEntities.clear();
			} else {
				closeEntities = new ArrayList<Integer>();
				closeEntitiesByEntity.put(entity, closeEntities);
			}
			for (int j = 0; j < entities.size(); j++) {
				int otherEntity = entities.get(j);
				if (otherEntity != entity) {
					TransformComponent othertTransformComponent = transformComponentMapper.get(otherEntity);
					reusableVector.set(othertTransformComponent.position.x - transformComponent.position.x, othertTransformComponent.position.y - transformComponent.position.y);
					if (reusableVector.lengthSquared() <= MAX_DISTANCE_SQUARED) {
						closeEntities.add(otherEntity);
					}
				}
			}
		}
	}
}
