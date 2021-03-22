package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

@PrefabData("com/vast/prefab/observer.json")
public class ObserverPrefab extends VastPrefab {
    public ObserverPrefab(World world) {
        super(world);
    }
}
