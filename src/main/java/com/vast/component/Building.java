package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Building extends Component {
	public String type = "unspecified";
}
