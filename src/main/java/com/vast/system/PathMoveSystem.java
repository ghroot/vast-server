package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.vast.Profiler;
import com.vast.component.Path;
import com.vast.component.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector2f;

@Profile(enabled = true, using = Profiler.class)
public class PathMoveSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(PathMoveSystem.class);

    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<Path> pathMapper;

    private final float MAX_PATHING_DURATION = 12.0f;
    private final float WALK_SPEED = 0.8f;
    private final float RUN_SPEED = 2.5f;

    private Vector2f reusableDirection;

    public PathMoveSystem() {
        super(Aspect.all(Transform.class, Path.class));
        reusableDirection = new Vector2f();
    }

	@Override
	protected void inserted(int entity) {
		pathMapper.get(entity).pathingTimeLeft = MAX_PATHING_DURATION;
	}

	@Override
    protected void process(int entity) {
        Transform transform = transformMapper.get(entity);
        Path path = pathMapper.get(entity);

        reusableDirection.set(path.targetPosition.x - transform.position.x, path.targetPosition.y - transform.position.y);
        if (reusableDirection.length() > 0) {
            float distance = reusableDirection.length();
            float speed = WALK_SPEED;
            if (distance > 2.0f) {
            	speed = RUN_SPEED;
			}
            reusableDirection.normalize();
            if (speed * world.delta > distance) {
                transform.position.set(path.targetPosition);
                pathMapper.remove(entity);
            } else {
                reusableDirection.scale(speed * world.delta);
                transform.position.add(reusableDirection);
            }
        }

        path.pathingTimeLeft -= world.delta;
        if (path.pathingTimeLeft <= 0.0f) {
        	pathMapper.remove(entity);
		}
    }
}
