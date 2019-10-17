package com.vast.component;

import com.artemis.PooledComponent;

import java.util.HashMap;
import java.util.Map;

public class Learn extends PooledComponent {
	public Map<String, Byte> words = new HashMap<>(); // TODO: Change to array(s)
	public float interval = 3f;
	public transient float countdown;

	@Override
	protected void reset() {
		words = new HashMap<>();
		interval = 3f;
		countdown = 0f;
	}
}
