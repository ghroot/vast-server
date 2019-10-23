package com.vast.component;

import org.junit.Assert;
import org.junit.Test;

public class TestTeach {
	@Test
	public void addWord() {
		Teach teach = new Teach();
		teach.words = new String[] {"first"};

		teach.addWord("test");

		Assert.assertArrayEquals(new String[] {"first", "test"}, teach.words);
	}

	@Test
	public void removeWord() {
		Teach teach = new Teach();
		teach.words = new String[] {"first", "second", "third"};

		teach.removeWord("second");

		Assert.assertArrayEquals(new String[] {"first", "third"}, teach.words);
	}
}
