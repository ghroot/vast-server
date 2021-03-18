package com.vast.monitor.model;

import com.vast.monitor.MonitorEntity;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

public class EntityModel implements TableModel {
    private MonitorEntity entity;

    private List<TableModelListener> listeners;

    public EntityModel() {
        listeners = new ArrayList<>();
    }

    public void refresh(MonitorEntity entity) {
        this.entity = entity;

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    public void clear() {
        refresh(null);
    }

    @Override
    public int getRowCount() {
        if (entity != null) {
            return entity.components.size();
        } else {
            return 1;
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Component";
        } else if (columnIndex == 1) {
            return "Details";
        } else {
            return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else if (columnIndex == 1) {
            return String.class;
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
        if (entity != null) {
            if (columnIndex == 0) {
                return entity.components.get(rowIndex).name;
            } else if (columnIndex == 1) {
                return entity.components.get(rowIndex).details;
            } else {
                return null;
            }
        } else {
            if (columnIndex == 0) {
                return "No entity selected";
            } else if (columnIndex == 1) {
                return "";
            } else {
                return null;
            }
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
