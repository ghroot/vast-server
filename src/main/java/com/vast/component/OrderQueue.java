package com.vast.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.vast.order.request.OrderRequest;

import java.util.LinkedList;
import java.util.Queue;

@Transient
public class OrderQueue extends PooledComponent {
    public Queue<OrderRequest> requests = new LinkedList<>();

    @Override
    protected void reset() {
        requests.clear();
    }
}
