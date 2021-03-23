package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.vast.order.request.OrderRequest;

import java.util.ArrayList;
import java.util.List;

@Transient
public class OrderQueue extends PooledComponent {
    public List<OrderRequest> requests = new ArrayList<>();

    @Override
    protected void reset() {
        requests.clear();
    }
}
