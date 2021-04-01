package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.vast.component.Scan;

public class ScanClearingSystem extends IteratingSystem {
    private ComponentMapper<Scan> scanMapper;

    public ScanClearingSystem() {
        super(Aspect.all(Scan.class));
    }

    @Override
    protected void process(int scanEntity) {
        Scan scan = scanMapper.get(scanEntity);

        scan.nearbyEntities.clear();
    }
}
