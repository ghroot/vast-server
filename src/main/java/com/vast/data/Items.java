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

public class Items {
	private static final Logger logger = LoggerFactory.getLogger(Items.class);

	private Map<Integer, Item> items;

	public Items() {
		try {
			items = new HashMap<Integer, Item>();
			JSONArray itemsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream("items.json"), Charset.defaultCharset()));
			for (Iterator<Object> it = itemsData.iterator(); it.hasNext();) {
				JSONObject itemData = (JSONObject) it.next();
				int type = itemData.getInt("type");
				String name = itemData.getString("name");
				items.put(type, new Item(type, name));
			}
		} catch (Exception exception) {
			logger.error("Error parsing items", exception);
		}
	}

	public Item getItem(int type) {
		return items.get(type);
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
