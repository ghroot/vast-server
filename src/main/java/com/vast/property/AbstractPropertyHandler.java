package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;

import java.util.HashMap;

public abstract class AbstractPropertyHandler<TPropertyData, TDataObjectData> implements PropertyHandler {
    public ComponentMapper<SyncHistory> syncHistoryMapper;

    private byte property;

    public AbstractPropertyHandler(byte property) {
        this.property = property;
    }

    @Override
    public byte getProperty() {
        return property;
    }

    protected abstract TPropertyData getPropertyData(int entity);

    protected boolean passedThresholdForSync(int propertyEntity, TPropertyData lastSyncedPropertyData) {
        return !getPropertyData(propertyEntity).equals(lastSyncedPropertyData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean decorateDataObject(int interestedEntity, int propertyEntity, DataObject dataObject, boolean force) {
        if (force || !syncHistoryMapper.has(interestedEntity) || !syncHistoryMapper.get(interestedEntity).hasSyncedPropertyData(propertyEntity, property) ||
                passedThresholdForSync(propertyEntity, (TPropertyData) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, property))) {
            TPropertyData propertyData = getPropertyData(propertyEntity);
            setDataObjectData(dataObject, convertPropertyDataToDataObjectData(propertyData));
            if (syncHistoryMapper.has(interestedEntity)) {
                setSyncHistoryData(interestedEntity, propertyEntity, propertyData);
            }
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    protected TDataObjectData convertPropertyDataToDataObjectData(TPropertyData propertyData) {
        return (TDataObjectData) propertyData;
    }

    private void setDataObjectData(DataObject dataObject, TDataObjectData dataObjectData) {
        dataObject.set(property, dataObjectData);
    }

    @SuppressWarnings("unchecked")
    protected TPropertyData getSyncHistoryData(int interestedEntity, int propertyEntity) {
        SyncHistory syncHistory = syncHistoryMapper.get(interestedEntity);
        if (syncHistory != null && syncHistory.syncedValues.containsKey(propertyEntity)) {
            return (TPropertyData) syncHistory.syncedValues.get(propertyEntity).get(property);
        } else {
            return null;
        }
    }

    protected void setSyncHistoryData(int interestedEntity, int propertyEntity, TPropertyData syncHistoryData) {
        SyncHistory syncHistory = syncHistoryMapper.get(interestedEntity);
        if (!syncHistory.syncedValues.containsKey(propertyEntity)) {
            syncHistory.syncedValues.put(propertyEntity, new HashMap<>());
        }
        syncHistory.syncedValues.get(propertyEntity).put(property, syncHistoryData);
    }
}
