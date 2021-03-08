package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Producer extends Component {
    public int recipeId = -1;
    public boolean producing = false;
    public float time = 0f;
}
