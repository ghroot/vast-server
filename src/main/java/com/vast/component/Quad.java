package com.vast.component;

import com.artemis.Component;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import net.mostlyoriginal.api.utils.QuadTree;

@Transient
@PooledWeaver
public class Quad extends Component {
    public QuadTree tree = null;
}
