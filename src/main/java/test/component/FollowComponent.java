package test.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.Transient;

@Transient
public class FollowComponent extends Component {
	@EntityId
	public int followingEntity = -1;
}
