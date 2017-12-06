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

public class Animals {
	private static final Logger logger = LoggerFactory.getLogger(Animals.class);

	private Map<Integer, Animal> animals;

	public Animals(Items items) {
		try {
			animals = new HashMap<Integer, Animal>();
			JSONArray animalsData = new JSONArray(IOUtils.toString(getClass().getResourceAsStream("animals.json"), Charset.defaultCharset()));
			for (Iterator<Object> it = animalsData.iterator(); it.hasNext();) {
				JSONObject animalData = (JSONObject) it.next();
				int id = -1;
				String name = null;
				Map<String, JSONObject> aspects = new HashMap<String, JSONObject>();
				for (String key : animalData.keySet()) {
					Object value = animalData.get(key);
					if (key.equals("id")) {
						id = (int) value;
					} else if (key.equals("name")) {
						name = (String) value;
					} else {
						aspects.put(key, (JSONObject) value);
					}
				}
				Animal animal = new Animal(id, name, aspects);
				animals.put(id, animal);
			}
		} catch (Exception exception) {
			logger.error("Error parsing animals", exception);
		}
	}

	public Animal getAnimal(int id) {
		return animals.get(id);
	}
}
