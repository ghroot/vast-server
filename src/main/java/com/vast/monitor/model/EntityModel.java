package com.vast.monitor.model;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityModel implements TableModel {
    private List<TableModelListener> listeners;
    private String[] componentNames;
    private String[] componentDetails;

    public int clickedEntity = -1;

    public EntityModel() {
        listeners = new ArrayList<>();
    }

    public void refresh(Map<String, String> entity) {
        if (entity != null) {
            int size = entity.size();
            if (componentNames == null || componentNames.length != size) {
                componentNames = new String[size];
            }
            if (componentDetails == null || componentDetails.length != size) {
                componentDetails = new String[size];
            }
            int index = 0;
            for (String componentName : entity.keySet()) {
                String componentDetail = entity.get(componentName);
                componentNames[index] = componentName;
                componentDetails[index] = componentDetail;
                index++;
            }
        } else {
            componentNames = null;
            componentDetails = null;
        }

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    @Override
    public int getRowCount() {
        if (componentNames != null) {
            return componentNames.length;
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
            return "Detail";
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
        String componentName = (String) getValueAt(rowIndex, 0);
        String componentDetail = (String) getValueAt(rowIndex, 1);
        if ("Observer".equals(componentName)) {
            clickedEntity = Integer.parseInt(componentDetail.split(",")[0].trim());
        } else if ("Observed".equals(componentName)) {
            clickedEntity = Integer.parseInt(componentDetail);
        } else if ("Parent".equals(componentName)) {
            clickedEntity = Integer.parseInt(componentDetail);
        } else if ("Interact".equals(componentName)) {
            clickedEntity = Integer.parseInt(componentDetail.split(",")[0].trim());
        } else if ("Used".equals(componentName)) {
            clickedEntity = Integer.parseInt(componentDetail);
        }
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (componentNames != null) {
            if (columnIndex == 0) {
                return componentNames[rowIndex];
            } else if (columnIndex == 1) {
                return componentDetails[rowIndex];
            }
        } else {
            if (columnIndex == 0) {
                return "No entity";
            } else if (columnIndex == 1) {
                return "";
            }
        }

        return null;
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
