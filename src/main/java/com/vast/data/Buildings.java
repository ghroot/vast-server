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

	public Buildings(Items items) {
		try {
			buildings = new HashMap<Integer, Building>();
			JSONArray buildingsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream("buildings.json"), Charset.defaultCharset()));
			for (Iterator<Object> it = buildingsData.iterator(); it.hasNext();) {
				JSONObject buildingData = (JSONObject) it.next();
				int id = -1;
				String name = null;
				Set<Cost> costs = new HashSet<Cost>();
				Map<String, JSONObject> aspects = new HashMap<String, JSONObject>();
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
		return new ArrayList<Building>(buildings.values());
	}

	public Building getBuilding(int id) {
		return buildings.get(id);
	}
}
