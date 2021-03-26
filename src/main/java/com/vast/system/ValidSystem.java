package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.network.Properties;

import javax.vecmath.Vector2f;

public class ValidSystem extends IteratingSystem {
    private ComponentMapper<Transform> transformMapper;
    private ComponentMapper<Scan> scanMapper;
    private ComponentMapper<Placeholder> placeholderMapper;
    private ComponentMapper<Sync> syncMapper;
    private ComponentMapper<Collision> collisionMapper;

    private float minimumDistance2;

    private Vector2f reusableVector;

    public ValidSystem(float minimumDistance) {
        super(Aspect.all(Placeholder.class, Scan.class, Transform.class));
        minimumDistance2 = minimumDistance * minimumDistance;

        reusableVector = new Vector2f();
    }

    @Override
    public void inserted(IntBag entities) {
    }

    @Override
    public void removed(IntBag entities) {
    }

    @Override
    protected void process(int placeholderEntity) {
        Transform transform = transformMapper.get(placeholderEntity);
        Scan scan = scanMapper.get(placeholderEntity);
        Placeholder placeholder = placeholderMapper.get(placeholderEntity);

        boolean previousValid = placeholder.valid;
        placeholder.valid = true;

        int[] nearbyEntities = scan.nearbyEntities.getData();
        for (int i = 0, size = scan.nearbyEntities.size(); i < size; ++i) {
            int nearbyEntity = nearbyEntities[i];
            if (collisionMapper.has(nearbyEntity)) {
                Transform nearbyTransform = transformMapper.get(nearbyEntity);
                reusableVector.set(transform.position.x - nearbyTransform.position.x,
                        transform.position.y - nearbyTransform.position.y);
                if (reusableVector.lengthSquared() < minimumDistance2) {
                    placeholder.valid = false;
                    break;
                }
            }
        }

        if (placeholder.valid != previousValid) {
            syncMapper.create(placeholderEntity).markPropertyAsDirty(Properties.VALID);
        }
    }
}
