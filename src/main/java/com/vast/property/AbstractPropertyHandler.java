package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;

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
        if (force || !syncHistoryMapper.has(interestedEntity) || passedThresholdForSync(propertyEntity,
                (TPropertyData) syncHistoryMapper.get(interestedEntity).getSyncedPropertyData(propertyEntity, property))) {
            TPropertyData propertyData = getPropertyData(propertyEntity);
            setDataObjectData(dataObject, convertPropertyDataToDataObjectData(propertyData));
            setSyncHistoryData(entity, propertyData);
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
