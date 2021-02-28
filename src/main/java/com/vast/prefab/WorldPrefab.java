package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

@PrefabData("com/vast/prefab/world.json")
public class WorldPrefab extends VastPrefab {
    public WorldPrefab(World world) {
        super(world);
    }
}
