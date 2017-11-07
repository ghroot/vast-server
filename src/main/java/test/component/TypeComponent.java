package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class TypeComponent extends Component {
	public String type = "unspecified";
}
