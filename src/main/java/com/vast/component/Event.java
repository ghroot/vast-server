package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

@Transient
public class Event extends PooledComponent {
	public List<EventEntry> entries = new ArrayList<>();

	@Override
	protected void reset() {
		entries.clear();
	}

	public EventEntry addEntry(String type) {
		EventEntry entry = new EventEntry(type);
		entries.add(entry);
		return entry;
	}

	public static class EventEntry {
		public String type;
		public Object data = null;
		public EventPropagation propagation;

		private EventEntry(String type) {
			this.type = type;
			propagation = EventPropagation.NEARBY;
		}

		public EventEntry setData(Object data) {
			this.data = data;
			return this;
		}

		public EventEntry setOwnerPropagation() {
			propagation = EventPropagation.OWNER;
			return this;
		}

		public EventEntry setAllPropagation() {
			propagation = EventPropagation.ALL;
			return this;
		}
	}

	public enum EventPropagation {
		OWNER,
		NEARBY,
		ALL
	}
}
