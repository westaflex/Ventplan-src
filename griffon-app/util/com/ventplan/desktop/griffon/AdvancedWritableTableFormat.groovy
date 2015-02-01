package com.ventplan.desktop.griffon

import ca.odell.glazedlists.gui.AdvancedTableFormat
import ca.odell.glazedlists.gui.WritableTableFormat

interface AdvancedWritableTableFormat extends AdvancedTableFormat, WritableTableFormat {

    // WritableTableFormat
    public int getColumnCount()

    public String getColumnName(int columnIndex)

    public Object getColumnValue(Object baseObject, int column)

    public boolean isEditable(Object baseObject, int column)

    public Object setColumnValue(Object baseObject, Object editedValue, int column)

    public Object getValueAt(int rowIndex, int columnIndex)

    public Class getColumnClass(int i)

    public Comparator getColumnComparator(int i)

}
