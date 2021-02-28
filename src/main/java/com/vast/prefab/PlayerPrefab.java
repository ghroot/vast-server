package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

@PrefabData("com/vast/prefab/player.json")
public class PlayerPrefab extends VastPrefab {
    public PlayerPrefab(World world) {
        super(world);
    }
}
