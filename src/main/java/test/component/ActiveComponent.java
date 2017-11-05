package test.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@PooledWeaver
@Transient
public class ActiveComponent extends Component {
}
