package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.vast.data.Recipe;

@Transient
@PooledWeaver
public class Craft extends Component {
	public Recipe recipe = null;
	public float craftTime = 0.0f;
}
