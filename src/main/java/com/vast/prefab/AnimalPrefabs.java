package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;

public class AnimalPrefabs {
    @PrefabData("com/vast/prefab/animal/rabbitAdult.json")
    public static class RabbitAdultPrefab extends VastPrefab {
        public RabbitAdultPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/animal/rabbitYoung.json")
    public static class RabbitYoungPrefab extends VastPrefab {
        public RabbitYoungPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/animal/deerAdult.json")
    public static class DeerAdultPrefab extends VastPrefab {
        public DeerAdultPrefab(World world) {
            super(world);
        }
    }

    @PrefabData("com/vast/prefab/animal/deerYoung.json")
    public static class DeerYoungPrefab extends VastPrefab {
        public DeerYoungPrefab(World world) {
            super(world);
        }
    }
}
