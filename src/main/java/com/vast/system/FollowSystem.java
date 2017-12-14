package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Follow;
import com.vast.component.Path;
import com.vast.component.Transform;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class FollowSystem extends IteratingSystem {
	private ComponentMapper<Follow> followMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Path> pathMapper;

	private Vector2f reusableVector;

	public FollowSystem() {
		super(Aspect.all(Follow.class, Transform.class));

		reusableVector = new Vector2f();
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	protected void removed(int entity) {
		pathMapper.remove(entity);
	}

	@Override
	protected void process(int entity) {
		Follow follow = followMapper.get(entity);

		if (follow.entity == -1) {
			followMapper.remove(entity);
		} else {
			Transform transform = transformMapper.get(entity);
			Transform otherTransform = transformMapper.get(follow.entity);

			reusableVector.set(otherTransform.position.x - transform.position.x, otherTransform.position.y - transform.position.y);
			float distance = reusableVector.length();
			if (distance > follow.distance) {
				reusableVector.normalize();
				reusableVector.scale(distance - follow.distance);
				Point2f targetPosition = pathMapper.create(entity).targetPosition;
				targetPosition.set(transform.position);
				targetPosition.add(reusableVector);
			}
		}
	}
}
