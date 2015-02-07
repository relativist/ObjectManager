package main;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * Created by rest on 2/7/15.
 */
public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount;

            // For single-click activation
            clickCount = 1;

            return ((MouseEvent)evt).getClickCount() >= clickCount;
        }
        return true;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return null;
    }
}