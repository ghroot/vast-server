package com.vast.component;

import com.artemis.PooledComponent;

import java.util.Arrays;

public class SkillEmission extends PooledComponent {
	public String[] words = new String[0];

	@Override
	protected void reset() {
		words = new String[0];
	}

	public void addWord(String word) {
		words = Arrays.copyOf(words, words.length + 1);
		words[words.length - 1] = word;
	}

	public void removeWord(String word) {
		String[] newWords = new String[words.length - 1];
		boolean skipped = false;
		for (int i = 0; i < words.length; i++) {
			if (!skipped && words[i].equals(word)) {
				skipped = true;
			} else {
				newWords[skipped ? i - 1 : i] = word;
			}
		}
		words = newWords;
	}
}
