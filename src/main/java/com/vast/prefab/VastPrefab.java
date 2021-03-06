package com.vast.prefab;

import com.artemis.World;
import com.artemis.prefab.JsonValuePrefabReader;
import com.artemis.prefab.Prefab;

public abstract class VastPrefab extends Prefab {
    public VastPrefab(World world) {
        super(world, new JsonValuePrefabReader());
    }

    public int createEntity() {
        return create().entities.get(0);
    }
}
