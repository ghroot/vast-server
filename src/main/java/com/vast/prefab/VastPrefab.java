package com.vast.prefab;

import com.artemis.World;
import com.artemis.prefab.JsonValuePrefabReader;
import com.artemis.prefab.Prefab;

public class VastPrefab extends Prefab {
    public VastPrefab(World world) {
        super(world, new JsonValuePrefabReader());
    }
}
