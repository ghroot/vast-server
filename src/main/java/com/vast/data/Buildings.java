package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Buildings {
	private static final Logger logger = LoggerFactory.getLogger(Buildings.class);

	private Map<Integer, Building> buildings;

	public Buildings(Items items) {
		try {
			buildings = new HashMap<Integer, Building>();
			JSONArray buildingsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream("buildings.json"), Charset.defaultCharset()));
			for (Iterator<Object> it = buildingsData.iterator(); it.hasNext();) {
				JSONObject buildingData = (JSONObject) it.next();
				int type = buildingData.getInt("type");
				String name = buildingData.getString("name");
				float buildDuration = buildingData.getFloat("buildDuration");
				Building building = new Building(type, name, buildDuration);
				JSONObject costData = buildingData.getJSONObject("cost");
				for (String itemName : costData.keySet()) {
					int amount = costData.getInt(itemName);
					building.addCost(new Cost(items.getItem(itemName).getType(), amount));
				}
				buildings.put(type, building);
			}
		} catch (Exception exception) {
			logger.error("Error parsing buildings", exception);
		}
	}

	public Building getBuilding(int type) {
		return buildings.get(type);
	}
}
