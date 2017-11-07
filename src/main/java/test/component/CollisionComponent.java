package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class CollisionComponent extends Component {
	public float radius = 0.2f;
}
