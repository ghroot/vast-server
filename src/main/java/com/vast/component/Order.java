package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Order extends Component {
	public enum Type {
		MOVE,
		INTERACT,
		BUILD
	}
	public Type type;
}
