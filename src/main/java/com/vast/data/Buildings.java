package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

public class Buildings {
	private static final Logger logger = LoggerFactory.getLogger(Buildings.class);

	private Map<Integer, Building> buildings;

	public Buildings() {
		buildings = new HashMap<>();
	}

	public Buildings(String fileName, Items items) {
		try {
			buildings = new HashMap<>();
			JSONArray buildingsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream(fileName),
					Charset.defaultCharset()));
			for (Object buildingsDatum : buildingsData) {
				JSONObject buildingData = (JSONObject) buildingsDatum;
				int id = -1;
				String name = null;
				Set<Cost> costs = new HashSet<>();
				Map<String, JSONObject> aspects = new HashMap<>();
				for (String key : buildingData.keySet()) {
					Object value = buildingData.get(key);
					if (key.equals("id")) {
						id = (int) value;
					} else if (key.equals("name")) {
						name = (String) value;
					} else {
						if (key.equals("constructable")) {
							JSONObject costData = ((JSONObject) value).getJSONObject("cost");
							for (String itemName : costData.keySet()) {
								int amount = costData.getInt(itemName);
								costs.add(new Cost(items.getItem(itemName).getId(), amount));
							}
						}
						aspects.put(key, (JSONObject) value);
					}
				}
				Building building = new Building(id, name, costs, aspects);
				buildings.put(id, building);
			}
		} catch (Exception exception) {
			logger.error("Error parsing buildings", exception);
		}
	}

	public List<Building> getAllBuildings() {
		return new ArrayList<>(buildings.values());
	}

	public Building getBuilding(int id) {
		return buildings.get(id);
	}
}
