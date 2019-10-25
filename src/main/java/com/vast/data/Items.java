package com.vast.data;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

public class Items {
	private static final Logger logger = LoggerFactory.getLogger(Items.class);

	private Map<Integer, Item> items;

	public Items() {
		try {
			items = new HashMap<>();
			JSONArray itemsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream("items.json"), Charset.defaultCharset()));
			for (Iterator<Object> it = itemsData.iterator(); it.hasNext();) {
				JSONObject itemData = (JSONObject) it.next();
				int id = itemData.getInt("id");
				JSONArray tagsArray = itemData.getJSONArray("tags");
				String[] tags = new String[tagsArray.length()];
				for (int i = 0; i < tags.length; i++) {
					tags[i] = tagsArray.optString(i);
				}
				String name = itemData.getString("name");
				Item item;
				if (itemData.has("craftable")) {
					Set<Cost> costs = new HashSet<>();
					float craftDuration = itemData.getJSONObject("craftable").getFloat("duration");
					JSONObject costData = itemData.getJSONObject("craftable").getJSONObject("cost");
					for (String itemName : costData.keySet()) {
						int amount = costData.getInt(itemName);
						costs.add(new Cost(getItem(itemName).getId(), amount));
					}
					item = new CraftableItem(id, tags, name, costs, craftDuration);
				} else {
					item = new Item(id, tags, name);
				}
				items.put(id, item);
			}
		} catch (Exception exception) {
			logger.error("Error parsing items", exception);
		}
	}

	public List<Item> getAllItems() {
		return new ArrayList<>(items.values());
	}

	public Item getItem(int id) {
		return items.get(id);
	}

	public Item getItem(String name) {
		for (Item item : items.values()) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		return null;
	}
}
