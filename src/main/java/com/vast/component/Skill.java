package com.vast.component;

import com.artemis.PooledComponent;

import java.util.Arrays;

public class Skill extends PooledComponent {
	public String[] words = new String[0];
	public byte[] wordLevels = new byte[0];
	public float interval = 3f;
	public transient float countdown;

	@Override
	protected void reset() {
		words = new String[0];
		wordLevels = new byte[0];
		interval = 3f;
		countdown = 0f;
	}

	public void increaseWordLevel(String word) {
		boolean handled = false;
		for (int i = 0; i < words.length; i++) {
			if (word.equals(words[i])) {
				wordLevels[i] = (byte) Math.min(100, wordLevels[i] + 1);
				handled = true;
				break;
			}
		}
		if (!handled) {
			words = Arrays.copyOf(words, words.length + 1);
			words[words.length - 1] = word;
			wordLevels = Arrays.copyOf(wordLevels, wordLevels.length + 1);
			wordLevels[wordLevels.length - 1] = (byte) 1;
		}
	}
}
