package com.vast.data;

public class SpatialHash {
	private int x;
	private int y;

	private int uniqueKey;

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
		updateUniqueKey();
	}

	public void setY(int y) {
		this.y = y;
		updateUniqueKey();
	}

	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
		updateUniqueKey();
	}

	private void updateUniqueKey() {
		long a = (long) (x >= 0 ? 2 * x : -2 * x - 1);
		long b = (long) (y >= 0 ? 2 * y : -2 * y - 1);
		int c = (int) ((a >= b ? a * a + a + b : a + b * b) / 2);
		uniqueKey = x < 0 && y < 0 || x >= 0 && y >= 0 ? c : -c - 1;
	}

	public int getUniqueKey() {
		return uniqueKey;
	}
}
