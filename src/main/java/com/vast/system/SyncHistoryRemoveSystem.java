package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Observer;
import com.vast.component.SyncHistory;

import java.util.ArrayList;
import java.util.List;

public class SyncHistoryRemoveSystem extends IteratingSystem {
    private ComponentMapper<Observer> observerMapper;
    private ComponentMapper<SyncHistory> syncHistoryMapper;

    private List<Integer> reusablePropertyEntitiesToRemove;

    public SyncHistoryRemoveSystem() {
        super(Aspect.all(Observer.class, SyncHistory.class));

        reusablePropertyEntitiesToRemove = new ArrayList<>();
    }

    @Override
    protected void process(int entity) {
        Observer observer = observerMapper.get(entity);
        SyncHistory syncHistory = syncHistoryMapper.get(entity);

        reusablePropertyEntitiesToRemove.clear();
        for (int propertyEntity : syncHistory.syncedValues.keySet()) {
            if (!observer.knowEntities.contains(propertyEntity)) {
                reusablePropertyEntitiesToRemove.add(propertyEntity);
            }
        }

        for (int propertyEntityToRemove : reusablePropertyEntitiesToRemove) {
            syncHistory.removeSyncedPropertyData(propertyEntityToRemove);
        }
    }
}
