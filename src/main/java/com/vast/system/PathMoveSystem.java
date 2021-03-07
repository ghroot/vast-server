package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Path;
import com.vast.component.Speed;
import com.vast.component.Sync;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class PathMoveSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(PathMoveSystem.class);

    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Sync> syncMapper;

	private float cancelMovementDuration;

    private Vector2f reusableVector;

    public PathMoveSystem(float cancelMovementDuration) {
        super(Aspect.all(Transform.class, Path.class, Speed.class));

        this.cancelMovementDuration = cancelMovementDuration;

        reusableVector = new Vector2f();
    }

    @Override
    protected void inserted(int entity) {
        Transform transform = transformMapper.get(entity);
        Path path = pathMapper.get(entity);
        path.lastPosition = new Point2f(transform.position);
    }

    @Override
	public void removed(IntBag entities) {
	}

	@Override
    protected void process(int entity) {
        Transform transform = transformMapper.get(entity);
        Path path = pathMapper.get(entity);
        Speed speed = speedMapper.get(entity);

        float moveDistance = speed.getModifiedSpeed() * world.delta;

        reusableVector.set(transform.position.x - path.lastPosition.x, transform.position.y - path.lastPosition.y);
        float distanceFromLastPosition = reusableVector.length();
        if (distanceFromLastPosition < moveDistance / 4) {
            path.timeInSamePosition += world.delta;
        } else {
            path.timeInSamePosition = 0f;
        }

        if (path.timeInSamePosition >= cancelMovementDuration) {
            logger.debug("Cancelling path movement for entity {}", entity);
            pathMapper.remove(entity);
        } else {
            reusableVector.set(path.targetPosition.x - transform.position.x, path.targetPosition.y - transform.position.y);
            float distanceToTargetPosition = reusableVector.length();
            reusableVector.normalize();

            if (moveDistance > distanceToTargetPosition) {
                transform.position.set(path.targetPosition);
                syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
                pathMapper.remove(entity);
            } else {
                transform.rotation = getAngle(reusableVector);
                syncMapper.create(entity).markPropertyAsDirty(Properties.ROTATION);

                path.lastPosition.set(transform.position);
                reusableVector.scale(moveDistance);
                transform.position.add(reusableVector);
                syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
            }
        }
	}

	public float getAngle(Vector2f direction) {
		float angle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}
}
