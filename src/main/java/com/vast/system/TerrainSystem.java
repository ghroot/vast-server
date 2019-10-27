package com.vast.system;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.vast.component.Terrain;
import com.vast.data.WorldConfiguration;

public class TerrainSystem extends BaseSystem {
	private ComponentMapper<Terrain> terrainMapper;

	@All(Terrain.class)
	private EntitySubscription terrainSubscription;

	private WorldConfiguration worldConfiguration;

	public TerrainSystem(WorldConfiguration worldConfiguration) {
		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void processSystem() {
	}
}
