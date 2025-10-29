// 파일 이름: ZebraTableCellRenderer.java
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ZebraTableCellRenderer extends DefaultTableCellRenderer {
    private static final Color ROW_EVEN_COLOR = Color.WHITE;
    private static final Color ROW_ODD_COLOR = new Color(245, 245, 245);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? ROW_EVEN_COLOR : ROW_ODD_COLOR);
        }
        return c;
    }
}