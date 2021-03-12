package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

public class BuildingPrefabs {
    @PrefabData("com/vast/prefab/building/placeholder.json")
    public static class PlaceholderPrefab extends VastPrefab {
        public PlaceholderPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/chest.json")
    public static class ChestPrefab extends BuildingPrefab {
        public ChestPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/fireplace.json")
    public static class FireplacePrefab extends BuildingPrefab {
        public FireplacePrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/planter.json")
    public static class PlanterPrefab extends BuildingPrefab {
        public PlanterPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/torch.json")
    public static class TorchPrefab extends BuildingPrefab {
        public TorchPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/wall.json")
    public static class WallPrefab extends BuildingPrefab {
        public WallPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/building/factory.json")
    public static class FactoryPrefab extends BuildingPrefab {
        public FactoryPrefab(World world) {
            super(world);
        }
    }
}
