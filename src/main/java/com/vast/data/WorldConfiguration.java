package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class WorldConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(WorldConfiguration.class);

	public static final int DEFAULT_DAY_DURATION = 8;
	public static final int DEFAULT_NIGHT_DURATION = 2;

	public int width;
	public int height;
	public int dayDuration;
	public int nightDuration;

	public WorldConfiguration(int width, int height) {
		this.width = width;
		this.height = height;

		dayDuration = DEFAULT_DAY_DURATION;
		nightDuration = DEFAULT_NIGHT_DURATION;
	}

	public WorldConfiguration(String fileName) {
		try {
			JSONObject worldData = new JSONObject(IOUtils.toString(getClass().getResourceAsStream(fileName), Charset.defaultCharset()));
			width = worldData.getInt("width");
			height = worldData.getInt("height");
			dayDuration = worldData.getInt("dayDuration");
			nightDuration = worldData.getInt("nightDuration");
		} catch (Exception exception) {
			logger.error("Error parsing world configuration", exception);
		}
	}
}
