package com.vast.monitor.model;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldInfoModel implements TableModel {
    private List<TableModelListener> listeners;
    private String[] names;
    private String[] values;

    public WorldInfoModel() {
        listeners = new ArrayList<>();
        names = new String[0];
        values = new String[0];
    }

    public void refresh(Map<String, String> worldInfo) {
        if (worldInfo != null) {
            int size = worldInfo.size();
            if (names == null || names.length != size) {
                names = new String[size];
            }
            if (values == null || values.length != size) {
                values = new String[size];
            }
            int index = 0;
            for (String name : worldInfo.keySet()) {
                names[index] = name;
                values[index] = worldInfo.get(name);
                index++;
            }
        } else {
            names = new String[0];
            values = new String[0];
        }

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    @Override
    public int getRowCount() {
        return names.length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Name";
        } else if (columnIndex == 1) {
            return "Value";
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return names[rowIndex];
        } else if (columnIndex == 1) {
            return values[rowIndex];
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
