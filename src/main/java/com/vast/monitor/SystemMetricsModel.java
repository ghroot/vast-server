package com.vast.monitor;

import com.artemis.BaseSystem;
import com.vast.data.Metrics;
import com.vast.data.SystemMetrics;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;
import java.util.stream.Collectors;

public class SystemMetricsModel implements TableModel {
    private Metrics metrics;

    private List<TableModelListener> listeners;
    private String[] systemNames;
    private int[] systemTimes;
    private int[] systemEntities;

    public SystemMetricsModel(Metrics metrics) {
        this.metrics = metrics;

        listeners = new ArrayList<>();
    }

    public void refresh() {
        Map<BaseSystem, SystemMetrics> systemMetricsToShow = metrics.getSystemMetrics().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing((BaseSystem system) -> system.getClass().getSimpleName())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        int size = systemMetricsToShow.size();
        if (systemNames == null || systemNames.length != size) {
            systemNames = new String[size];
        }
        if (systemTimes == null || systemTimes.length != size) {
            systemTimes = new int[size];
        }
        if (systemEntities == null || systemEntities.length != size) {
            systemEntities = new int[size];
        }
        int index = 0;
        for (BaseSystem system : systemMetricsToShow.keySet()) {
            SystemMetrics systemMetrics = metrics.getSystemMetrics().get(system);
            systemNames[index] = system.getClass().getSimpleName();
            systemTimes[index] = systemMetrics.getProcessingTime();
            systemEntities[index] = Math.max(systemMetrics.getNumberOfEntitiesInSystem(), 0);
            index++;
        }

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    @Override
    public int getRowCount() {
        return systemNames != null ? systemNames.length : 0;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "System";
        } else if (columnIndex == 1) {
            return "Time";
        } else if (columnIndex == 2) {
            return "Entities";
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else if (columnIndex == 1) {
            return Integer.class;
        } else if (columnIndex == 2) {
            return Integer.class;
        } else {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return systemNames[rowIndex];
        } else if (columnIndex == 1) {
            return systemTimes[rowIndex];
        } else if (columnIndex == 2) {
            return systemEntities[rowIndex];
        } else {
            return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}
