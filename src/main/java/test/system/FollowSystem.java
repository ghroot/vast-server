package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import test.component.FollowComponent;
import test.component.PathComponent;
import test.component.TransformComponent;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class FollowSystem extends IteratingSystem {
	private final float FOLLOW_DISTANCE = 1.5f;

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<FollowComponent> followComponentMapper;
	private ComponentMapper<PathComponent> pathComponentMapper;

	private Vector2f reusableVector;

	public FollowSystem() {
		super(Aspect.all(TransformComponent.class, FollowComponent.class));
		reusableVector = new Vector2f();
	}

	@Override
	protected void process(int entity) {
		FollowComponent followComponent = followComponentMapper.get(entity);
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		TransformComponent followTransformComponent = transformComponentMapper.get(followComponent.followingEntity);
		reusableVector.set(followTransformComponent.position.x - transformComponent.position.x, followTransformComponent.position.y - transformComponent.position.y);
		if (reusableVector.length() > FOLLOW_DISTANCE) {
			float distance = reusableVector.length();
			reusableVector.normalize();
			reusableVector.scale(distance - FOLLOW_DISTANCE);
			Point2f movePosition = new Point2f(transformComponent.position);
			movePosition.add(reusableVector);
			pathComponentMapper.create(entity).targetPosition = new Point2f(movePosition.x - 0.5f + (float) Math.random() * 1.0f, movePosition.y - 0.5f + (float) Math.random() * 1.0f);
		}
	}
}
