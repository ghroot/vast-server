package com.vast.prefab;

import com.artemis.World;
import com.artemis.annotations.PrefabData;
import com.artemis.prefab.JsonValuePrefabReader;
import com.artemis.utils.reflect.ClassReflection;

public class BuildingPrefab extends VastPrefab {
    private int subType;

    public BuildingPrefab(World world) {
        super(world);

        JsonValuePrefabReader reader = new JsonValuePrefabReader();
        reader.initialize(getPrefabDataPath());
        subType = reader.getData().get("entities").get("0").get("components").get("SubType").getInt("subType");
    }

    public int getSubType() {
        return subType;
    }

    private String getPrefabDataPath() {
        PrefabData pd = ClassReflection.getAnnotation(getClass(), PrefabData.class);
        if (pd != null) {
            return pd.value();
        } else {
            return null;
        }
    }
}
