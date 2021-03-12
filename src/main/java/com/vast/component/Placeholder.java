package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;

@PooledWeaver
public class Placeholder extends Component {
    public transient boolean valid = true;
}
