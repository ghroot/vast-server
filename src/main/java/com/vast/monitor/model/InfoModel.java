package com.vast.monitor.model;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoModel<T> implements TableModel {
    private String[] columnNames;
    private Class<T> valueClass;

    private List<TableModelListener> listeners;
    private String[] names;
    private List<T> values;

    public InfoModel(String[] columnNames, Class<T> valueClass) {
        this.columnNames = columnNames;
        this.valueClass = valueClass;

        listeners = new ArrayList<>();
        names = new String[0];
        values = new ArrayList<>();
    }

    public void refresh(Map<String, T> info) {
        if (info != null) {
            int size = info.size();
            if (names == null || names.length != size) {
                names = new String[size];
            }
            values.clear();
            int index = 0;
            for (String name : info.keySet()) {
                names[index] = name;
                values.add(info.get(name));
                index++;
            }
        } else {
            names = new String[0];
            values.clear();
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
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else {
            return valueClass;
        }
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
            return values.get(rowIndex);
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
