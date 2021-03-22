package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

@PrefabData("com/vast/prefab/avatar.json")
public class AvatarPrefab extends VastPrefab {
    public AvatarPrefab(World world) {
        super(world);
    }
}
