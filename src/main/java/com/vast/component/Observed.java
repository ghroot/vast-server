package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;

@Transient
@PooledWeaver
public class Observed extends Component {
    @EntityId public int observerEntity = -1;
}
