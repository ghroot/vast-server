package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

public class TerrainPrefabs {
    @PrefabData("com/vast/prefab/terrain/tree.json")
    public static class TreePrefab extends VastPrefab {
        public TreePrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/terrain/rock.json")
    public static class RockPrefab extends VastPrefab {
        public RockPrefab(World world) {
            super(world);
        }
    }
}