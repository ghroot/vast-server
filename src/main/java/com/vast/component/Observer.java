package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.utils.IntBag;
import com.vast.network.VastPeer;

public class Observer extends PooledComponent {
    public VastPeer peer;
    @EntityId public int observedEntity = -1;
    @EntityId
    public IntBag knowEntities = new IntBag();

    @Override
    protected void reset() {
        peer = null;
        observedEntity = -1;
        knowEntities.clear();
    }
}
