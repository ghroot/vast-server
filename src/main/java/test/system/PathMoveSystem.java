package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import test.Profiler;
import test.component.PathComponent;
import test.component.TransformComponent;

import javax.vecmath.Vector2f;

@Profile(enabled = true, using = Profiler.class)
public class PathMoveSystem extends IteratingSystem {
    private ComponentMapper<TransformComponent> transformComponentMapper;
    private ComponentMapper<PathComponent> pathComponentMapper;

    private final float MAX_PATHING_DURATION = 12.0f;
    private final float WALK_SPEED = 0.8f;
    private final float RUN_SPEED = 2.5f;

    private Vector2f reusableDirection;

    public PathMoveSystem() {
        super(Aspect.all(TransformComponent.class, PathComponent.class));
        reusableDirection = new Vector2f();
    }

	@Override
	protected void inserted(int entity) {
		pathComponentMapper.get(entity).pathingTimeLeft = MAX_PATHING_DURATION;
	}

	@Override
    protected void process(int entity) {
        TransformComponent transformComponent = transformComponentMapper.get(entity);
        PathComponent pathComponent = pathComponentMapper.get(entity);

        reusableDirection.set(pathComponent.targetPosition.x - transformComponent.position.x, pathComponent.targetPosition.y - transformComponent.position.y);
        if (reusableDirection.length() > 0) {
            float distance = reusableDirection.length();
            float speed = WALK_SPEED;
            if (distance > 2.0f) {
            	speed = RUN_SPEED;
			}
            reusableDirection.normalize();
            if (speed * world.delta > distance) {
                transformComponent.position.set(pathComponent.targetPosition);
                pathComponentMapper.remove(entity);
            } else {
                reusableDirection.scale(speed * world.delta);
                transformComponent.position.add(reusableDirection);
            }
        }

        pathComponent.pathingTimeLeft -= world.delta;
        if (pathComponent.pathingTimeLeft <= 0.0f) {
        	pathComponentMapper.remove(entity);
		}
    }
}
