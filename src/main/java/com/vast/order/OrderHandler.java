package com.vast.order;

import com.nhnent.haste.protocol.data.DataObject;

public interface OrderHandler {
	void initialize();
	boolean handlesMessageCode(short messageCode);
	boolean isOrderComplete(int orderEntity);
	void cancelOrder(int orderEntity);
	boolean startOrder(int orderEntity, short messageCode, DataObject dataObject);
	boolean modifyOrder(int orderEntity, short messageCode, DataObject dataObject);
}
