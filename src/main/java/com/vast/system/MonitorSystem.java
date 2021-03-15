package com.vast.system;

import com.artemis.Aspect;
import com.artemis.systems.IntervalSystem;
import com.vast.VastWorld;
import com.vast.data.Metrics;
import com.vast.data.WorldConfiguration;
import com.vast.monitor.Monitor;
import com.vast.network.VastPeer;

import java.util.Map;

public class MonitorSystem extends IntervalSystem {
    private Map<String, VastPeer> peers;
    private Metrics metrics;
    private WorldConfiguration worldConfiguration;
    private VastWorld vastWorld;

    private Monitor monitor;

    public MonitorSystem(Map<String, VastPeer> peers, Metrics metrics, WorldConfiguration worldConfiguration, VastWorld vastWorld) {
        super(Aspect.all(), 1f / 30);
        this.peers = peers;
        this.metrics = metrics;
        this.worldConfiguration = worldConfiguration;
        this.vastWorld = vastWorld;
    }

    @Override
    protected void initialize() {
        monitor = new Monitor(worldConfiguration, metrics);
    }

    @Override
    protected void dispose() {
        monitor.dispose();
    }

    @Override
    protected void processSystem() {
        monitor.sync(vastWorld);
    }
}
