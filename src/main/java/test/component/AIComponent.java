package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class AIComponent extends Component {
	public int countdown = 0;
}
