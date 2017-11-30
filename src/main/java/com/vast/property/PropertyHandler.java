package com.vast.property;

import com.nhnent.haste.protocol.data.DataObject;

public interface PropertyHandler {
	int getProperty();
	void decorateDataObject(int entity, DataObject dataObject, boolean force);
}
