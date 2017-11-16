package com.vast.order;

import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Order;

public interface OrderHandler {
	void initialize();
	short getMessageCode();
	Order.Type getOrderType();
	boolean isOrderComplete(int orderEntity);
	void cancelOrder(int orderEntity);
	boolean startOrder(int orderEntity, DataObject dataObject);
}
