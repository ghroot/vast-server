package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;

public abstract class AbstractPropertyHandler<TPropertyData, TDataObjectData> implements PropertyHandler {
    protected ComponentMapper<SyncHistory> syncHistoryMapper;

    private byte property;

    public AbstractPropertyHandler(byte property) {
        this.property = property;
    }

    // TODO: Protected? Private!?
    @Override
    public byte getProperty() {
        return property;
    }

    protected abstract boolean isInterestedIn(int entity);
    protected abstract TPropertyData getPropertyData(int entity);

    protected boolean passedThresholdForSync(int entity, TPropertyData lastSyncedPropertyData) {
        return !getPropertyData(entity).equals(lastSyncedPropertyData);
    }

    @Override
    public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
        if (isInterestedIn(entity)) {
            if (force || !hasSyncHistory(entity) || passedThresholdForSync(entity, getSyncHistoryData(entity))) {
                TPropertyData propertyData = getPropertyData(entity);
                setDataObjectData(dataObject, convertPropertyDataToDataObjectData(propertyData));
                setSyncHistoryData(entity, propertyData);
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    protected TDataObjectData convertPropertyDataToDataObjectData(TPropertyData propertyData) {
        return (TDataObjectData) propertyData;
    }

    private void setDataObjectData(DataObject dataObject, TDataObjectData data) {
        dataObject.set(property, data);
    }

    protected boolean hasSyncHistory(int entity) {
        SyncHistory syncHistory = syncHistoryMapper.get(entity);
        return syncHistory != null && syncHistory.syncedValues.containsKey(property);
    }

    @SuppressWarnings("unchecked")
    protected TPropertyData getSyncHistoryData(int entity) {
        SyncHistory syncHistory = syncHistoryMapper.get(entity);
        if (syncHistory != null && syncHistory.syncedValues.containsKey(property)) {
            return (TPropertyData) syncHistory.syncedValues.get(property);
        } else {
            return null;
        }
    }

    protected void setSyncHistoryData(int entity, TPropertyData syncHistoryData) {
        SyncHistory syncHistory = syncHistoryMapper.get(entity);
        if (syncHistory != null) {
            syncHistory.syncedValues.put(property, syncHistoryData);
        }
    }
}
