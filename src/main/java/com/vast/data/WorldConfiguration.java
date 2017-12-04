package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class WorldConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(WorldConfiguration.class);

	public int width;
	public int height;
	public int sectionSize;
	public int numberOfAIs;
	public int numberOfAnimals;

	public WorldConfiguration() {
		try {
			JSONObject worldData = new JSONObject(IOUtils.toString(getClass().getResourceAsStream("world.json"), Charset.defaultCharset()));
			width = worldData.getInt("width");
			height = worldData.getInt("height");
			sectionSize = worldData.getInt("sectionSize");
			numberOfAIs = worldData.getInt("numberOfAIs");
			numberOfAnimals = worldData.getInt("numberOfAnimals");
		} catch (Exception exception) {
			logger.error("Error parsing world configuration", exception);
		}
	}
}
