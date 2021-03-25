package com.vast.property;

import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;

public interface PropertyHandler {
	byte getProperty();
	boolean isInterestedIn(int entity);
	boolean decorateDataObject(int interestedEntity, int propertyEntity, DataObject dataObject, boolean force);
}
