package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Player extends Component {
    public transient long id = -1;
    public String name = null;
}
