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

import javax.vecmath.Vector2f;

public class PathMoveSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(PathMoveSystem.class);

    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Sync> syncMapper;

    private Vector2f reusableDirection;

    public PathMoveSystem() {
        super(Aspect.all(Transform.class, Path.class, Speed.class));
        reusableDirection = new Vector2f();
    }

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
    protected void process(int entity) {
        Transform transform = transformMapper.get(entity);
        Path path = pathMapper.get(entity);
        Speed speed = speedMapper.get(entity);

        reusableDirection.set(path.targetPosition.x - transform.position.x, path.targetPosition.y - transform.position.y);
		float distance = reusableDirection.length();
        if (distance > 0) {
			transform.rotation = getAngle(reusableDirection);
			syncMapper.create(entity).markPropertyAsDirty(Properties.ROTATION);

            reusableDirection.normalize();
            if (speed.getModifiedSpeed() * world.delta > distance) {
                transform.position.set(path.targetPosition);
                pathMapper.remove(entity);
            } else {
                reusableDirection.scale(speed.getModifiedSpeed() * world.delta);
                transform.position.add(reusableDirection);
            }
			syncMapper.create(entity).markPropertyAsDirty(Properties.POSITION);
        }
	}

	public float getAngle(Vector2f direction) {
		float angle = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
		if (angle < 0){
			angle += 360;
		}
		return angle;
	}
}
