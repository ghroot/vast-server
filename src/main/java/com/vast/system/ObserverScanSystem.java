package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Observer;
import com.vast.component.Scan;

public class ObserverScanSystem extends IteratingSystem {
    private ComponentMapper<Observer> observerMapper;
    private ComponentMapper<Scan> scanMapper;

    public ObserverScanSystem() {
        super(Aspect.all(Observer.class, Scan.class));
    }

    @Override
    protected void process(int observerEntity) {
        Observer observer = observerMapper.get(observerEntity);
        Scan scan = scanMapper.get(observerEntity);

        if (!scan.nearbyEntities.contains(observerEntity)) {
            scan.nearbyEntities.add(observerEntity);
        }

        if (!scan.nearbyEntities.contains(observer.observedEntity)) {
            scan.nearbyEntities.add(observer.observedEntity);
        }
    }
}
