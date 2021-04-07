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

	private List<Item> items;
	private Map<Integer, Item> itemsById;
	private Map<String, Item> itemsByName;

	public Items() {
		items = new ArrayList<>();
		itemsById = new HashMap<>();
		itemsByName = new HashMap<>();
	}

	public Items(String fileName) {
		try {
			JSONArray itemsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream(fileName), Charset.defaultCharset()));
			items = new ArrayList<>();
			itemsById = new HashMap<>();
			itemsByName = new HashMap<>();
			for (int i = 0; i < itemsData.length(); i++) {
				JSONObject itemData = (JSONObject) itemsData.get(i);
				int id = itemData.getInt("id");
				JSONArray tagsArray = itemData.getJSONArray("tags");
				Set<String> tags = new HashSet<>();
				for (int j = 0; j < tagsArray.length(); j++) {
					tags.add(tagsArray.optString(j));
				}
				String name = itemData.getString("name");
				Item item = new Item(id, tags, name);
				items.add(item);
				itemsById.put(id, item);
				itemsByName.put(name, item);
			}
		} catch (Exception exception) {
			logger.error("Error parsing items", exception);
		}
	}

	public List<Item> getAllItems() {
		return items;
	}

	public Item getItem(int id) {
		return itemsById.get(id);
	}

	public Item getItem(String name) {
		return itemsByName.get(name);
	}
}
