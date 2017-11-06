package test.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class AIComponent extends Component {
	public float countdown = 0.0f;
	@EntityId
	public int followingEntity = -1;
}
