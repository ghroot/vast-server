package test.component;

import com.artemis.PooledComponent;

import javax.vecmath.Point2f;

public class TransformComponent extends PooledComponent {
	public Point2f position = new Point2f();

	@Override
	protected void reset() {
		position = new Point2f();
	}
}
