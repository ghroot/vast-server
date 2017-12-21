package com.vast.data;

import javax.vecmath.Point2i;

public class SpatialHash extends Point2i {
	public int uniqueKey() {
		long a = (long) (x >= 0 ? 2 * x : -2 * x - 1);
		long b = (long) (y >= 0 ? 2 * y : -2 * y - 1);
		int c = (int) ((a >= b ? a * a + a + b : a + b * b) / 2);
		return x < 0 && y < 0 || x >= 0 && y >= 0 ? c : -c - 1;
	}
}
