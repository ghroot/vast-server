package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Player extends Component {
    public transient long id = 0;
    public String name = "";
}
