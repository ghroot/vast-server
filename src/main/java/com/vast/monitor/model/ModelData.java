package com.vast.monitor.model;

import com.artemis.BaseSystem;
import com.vast.data.SystemMetrics;
import com.vast.monitor.MonitorEntity;

import java.util.Map;

public class ModelData {
    public Map<String, String> worldInfo;
    public Map<BaseSystem, SystemMetrics> systemMetrics;
    public MonitorEntity entity;
}