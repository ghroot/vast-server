package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

public class PickupPrefabs {
    @PrefabData("com/vast/prefab/pickup/harvestedResources.json")
    public static class HarvestedResourcesTemplate extends VastPrefab {
        public HarvestedResourcesTemplate(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/pickup/stonePile.json")
    public static class StonePileTemplate extends VastPrefab {
        public StonePileTemplate(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/pickup/woodPile.json")
    public static class WoodPileTemplate extends VastPrefab {
        public WoodPileTemplate(World world) {
            super(world);
        }
    }
}
