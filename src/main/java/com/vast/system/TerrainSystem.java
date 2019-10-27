package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;
import com.vast.network.MessageCodes;

public class TerrainSystem extends IteratingSystem {
	private ComponentMapper<Terrain> terrainMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Active> activeMapper;

	@All(Terrain.class)
	private EntitySubscription terrainSubscription;

	private WorldConfiguration worldConfiguration;

	private EventMessage reusableEventMessage;

	public TerrainSystem(WorldConfiguration worldConfiguration) {
		super(Aspect.all(Player.class, Active.class, Scan.class));
		this.worldConfiguration = worldConfiguration;

		reusableEventMessage = new EventMessage(MessageCodes.TERRAIN);
	}

	@Override
	protected void inserted(int activePlayerEntity) {
		Terrain terrain = terrainMapper.get(terrainSubscription.getEntities().get(0));
		Transform playerTransform = transformMapper.get(activePlayerEntity);
		Scan playerScan = scanMapper.get(activePlayerEntity);
		Active playerActive = activeMapper.get(activePlayerEntity);

		int startCellX = (int) Math.floor((playerTransform.position.x - playerScan.distance) / worldConfiguration.cellSize);
		int startCellY = (int) Math.floor((playerTransform.position.y - playerScan.distance) / worldConfiguration.cellSize);
		int endCellX = (int) Math.floor((playerTransform.position.x + playerScan.distance) / worldConfiguration.cellSize);
		int endCellY = (int) Math.floor((playerTransform.position.y + playerScan.distance) / worldConfiguration.cellSize);
		int numberOfColumns = endCellX - startCellX;
		int numberOfRows = endCellY - startCellY;
		byte[] data = new byte[numberOfColumns * numberOfRows];
		int index = 0;
		for (int cellY = startCellY; cellY < endCellY; cellY++) {
			for (int cellX = startCellX; cellX < endCellX; cellX++) {
				data[index++] = terrain.cells[cellX][cellY];
			}
		}

		reusableEventMessage.getDataObject().clear();
		reusableEventMessage.getDataObject().set(MessageCodes.TERRAIN_ANCHOR_POSITION, new float[] {
			startCellX * worldConfiguration.cellSize,
			startCellY * worldConfiguration.cellSize
		});
		reusableEventMessage.getDataObject().set(MessageCodes.TERRAIN_DATA, data);
		playerActive.peer.send(reusableEventMessage);
	}

	@Override
	protected void process(int activePlayerEntity) {

	}
}
