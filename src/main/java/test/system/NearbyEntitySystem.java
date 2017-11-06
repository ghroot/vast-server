package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IntervalIteratingSystem;
import com.artemis.utils.IntBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.component.TransformComponent;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NearbyEntitySystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(NearbyEntitySystem.class);

	private final float MAX_DISTANCE = 2.0f;
	private final float MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

	private ComponentMapper<TransformComponent> transformComponentMapper;

	private Map<Integer, List<Integer>> nearbyEntitiesByEntity;

	private Vector2f reusableVector;

	public NearbyEntitySystem(Map<Integer, List<Integer>> nearbyEntitiesByEntity) {
		super(Aspect.all(TransformComponent.class), 100);
		this.nearbyEntitiesByEntity = nearbyEntitiesByEntity;
		reusableVector = new Vector2f();
	}

	@Override
	protected void process(int entity) {
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		List<Integer> nearbyEntities;
		if (nearbyEntitiesByEntity.containsKey(entity)) {
			nearbyEntities = nearbyEntitiesByEntity.get(entity);
			nearbyEntities.clear();
		} else {
			nearbyEntities = new ArrayList<Integer>();
			nearbyEntitiesByEntity.put(entity, nearbyEntities);
		}
		IntBag entities = world.getAspectSubscriptionManager().get(Aspect.all(TransformComponent.class)).getEntities();
		for (int i = 0; i < entities.size(); i++) {
			int otherEntity = entities.get(i);
			TransformComponent otherTransformComponent = transformComponentMapper.get(otherEntity);
			reusableVector.set(otherTransformComponent.position.x - transformComponent.position.x, otherTransformComponent.position.y - transformComponent.position.y);
			if (reusableVector.lengthSquared() <= MAX_DISTANCE_SQUARED) {
				nearbyEntities.add(otherEntity);
			}
		}
	}
}
