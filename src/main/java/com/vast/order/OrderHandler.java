package com.vast.order;

import com.nhnent.haste.protocol.data.DataObject;

public interface OrderHandler {
	void initialize();
	short getMessageCode();
	boolean isOrderComplete(int orderEntity);
	void cancelOrder(int orderEntity);
	boolean startOrder(int orderEntity, DataObject dataObject);
}
