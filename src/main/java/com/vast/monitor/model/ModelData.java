package com.vast.monitor.model;

import com.artemis.BaseSystem;
import com.vast.data.SystemMetrics;

import java.util.Map;

public class ModelData {
    public Map<String, String> worldInfo;
    public Map<String, String> selectedEntity;
    public Map<BaseSystem, SystemMetrics> systemMetrics;
    public Map<String, Integer> propertyInfo;
    public Map<String, String> messagesInfo;
    public Map<String, Integer> eventsInfo;
}
