package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@PooledWeaver
@Transient
public class Delete extends Component {
	public String reason = "unknown";
}
