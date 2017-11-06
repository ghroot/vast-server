package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import test.component.PathComponent;
import test.component.TransformComponent;

import javax.vecmath.Vector2f;

public class PathMoveSystem extends IteratingSystem {
    private ComponentMapper<TransformComponent> transformComponentMapper;
    private ComponentMapper<PathComponent> pathComponentMapper;

    private float speed;
    private Vector2f reusableDirection;

    public PathMoveSystem() {
        super(Aspect.all(TransformComponent.class, PathComponent.class));
        speed = 0.3f;
        reusableDirection = new Vector2f();
    }

    @Override
    protected void process(int entity) {
        TransformComponent transformComponent = transformComponentMapper.get(entity);
        PathComponent pathComponent = pathComponentMapper.get(entity);

        reusableDirection.set(pathComponent.targetPosition.x - transformComponent.position.x, pathComponent.targetPosition.y - transformComponent.position.y);
        if (reusableDirection.length() > 0) {
            float distance = reusableDirection.length();
            reusableDirection.normalize();
            if (speed > distance) {
                transformComponent.position.set(pathComponent.targetPosition);
                pathComponentMapper.remove(entity);
            } else {
                reusableDirection.scale(speed);
                transformComponent.position.add(reusableDirection);
            }
        }
    }
}
