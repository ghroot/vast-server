package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.vast.data.EntityRecipe;
import com.vast.data.Recipe;

@Transient
@PooledWeaver
public class Build extends Component {
    @EntityId public int placeholderEntity = -1;
    public EntityRecipe recipe = null;
}
