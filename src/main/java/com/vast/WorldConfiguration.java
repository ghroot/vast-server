package com.vast;

import java.util.Properties;

public class WorldConfiguration {
	public int width;
	public int height;
	public int sectionSize;
	public int numberOfTrees;
	public int numberOfAIs;

	public WorldConfiguration(Properties worldProperties) {
		this.width = Integer.parseInt(worldProperties.getProperty("width"));
		this.height = Integer.parseInt(worldProperties.getProperty("height"));
		this.sectionSize = Integer.parseInt(worldProperties.getProperty("sectionSize"));
		this.numberOfTrees = Integer.parseInt(worldProperties.getProperty("numberOfTrees"));
		this.numberOfAIs = Integer.parseInt(worldProperties.getProperty("numberOfAIs"));
	}
}
