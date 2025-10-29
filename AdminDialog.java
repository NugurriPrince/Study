// 파일 이름: AdminDialog.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class AdminDialog extends JDialog {
    private MainAppFrame parentFrame; private List<User> users; private List<Item> items;
    private JTable userTable; private JTable itemTable;
    private DefaultTableModel userTableModel; private DefaultTableModel itemTableModel;
    private static final Color DK_BLUE = new Color(0, 44, 122);
    private static final Color BG_LIGHT_GRAY = new Color(245, 245, 245);

    public AdminDialog(MainAppFrame parent, List<User> users, List<Item> items) {
        super(parent, "관리자 패널", true);
        this.parentFrame = parent; this.users = users; this.items = items;
        setSize(800, 600); setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        tabbedPane.addTab("사용자 관리", createUserManagementPanel());
        tabbedPane.addTab("물품 관리", createItemManagementPanel());
        
        add(tabbedPane, BorderLayout.CENTER); setLocationRelativeTo(parent);
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), title);
        border.setTitleColor(DK_BLUE); border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 12));
        return border;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        table.setDefaultRenderer(Object.class, new ZebraTableCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setBackground(DK_BLUE);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(100, 32));
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(new EmptyBorder(10, 10, 10, 10)); panel.setBackground(BG_LIGHT_GRAY);
        JPanel addUserPanel = new JPanel(new GridBagLayout());
        addUserPanel.setBorder(createTitledBorder("신규 사용자 추가"));
        addUserPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5);
        JTextField userIdField = new JTextField(10); JTextField userNameField = new JTextField(10); JTextField userPassField = new JTextField(10);
        JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"Student", "Staff", "Admin"}); JButton addUserButton = new JButton("추가");
        addUserButton.setBackground(DK_BLUE); addUserButton.setForeground(Color.BLACK);
        
        gbc.gridx = 0; gbc.gridy = 0; addUserPanel.add(new JLabel("ID:"), gbc); gbc.gridx = 1; gbc.gridy = 0; addUserPanel.add(userIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addUserPanel.add(new JLabel("이름:"), gbc); gbc.gridx = 1; gbc.gridy = 1; addUserPanel.add(userNameField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; addUserPanel.add(new JLabel("비밀번호:"), gbc); gbc.gridx = 3; gbc.gridy = 0; addUserPanel.add(userPassField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; addUserPanel.add(new JLabel("타입:"), gbc); gbc.gridx = 3; gbc.gridy = 1; addUserPanel.add(userTypeCombo, gbc);
        gbc.gridx = 4; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; addUserPanel.add(addUserButton, gbc);
        panel.add(addUserPanel, BorderLayout.NORTH);
        
        String[] userColumns = {"ID", "이름", "타입"};
        userTableModel = new DefaultTableModel(userColumns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; }};
        userTable = new JTable(userTableModel);
        styleTable(userTable);
        refreshUserTable();
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(createTitledBorder("사용자 목록"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton deleteUserButton = new JButton("선택한 사용자 삭제");
        deleteUserButton.setBackground(new Color(225, 225, 225)); deleteUserButton.setForeground(Color.RED.darker()); deleteUserButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        panel.add(deleteUserButton, BorderLayout.SOUTH);
        
        addUserButton.addActionListener(e -> {
            String newUserId = userIdField.getText().trim();
            if (newUserId.isEmpty()) { JOptionPane.showMessageDialog(this, "사용자 ID를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE); return; }
            boolean idExists = users.stream().anyMatch(user -> user.getId().equals(newUserId));
            if(idExists) { JOptionPane.showMessageDialog(this, "이미 존재하는 사용자 ID입니다.", "중복 오류", JOptionPane.ERROR_MESSAGE); return; }
            users.add(new User(newUserId, userNameField.getText(), (String)userTypeCombo.getSelectedItem(), userPassField.getText()));
            JOptionPane.showMessageDialog(this, "사용자가 추가되었습니다.");
            userIdField.setText(""); userNameField.setText(""); userPassField.setText("");
            refreshUserTable();
        });
        deleteUserButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "삭제할 사용자를 목록에서 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE); return; }
            String userIdToDelete = (String) userTableModel.getValueAt(selectedRow, 0);
            if ("admin".equalsIgnoreCase(userIdToDelete)) { JOptionPane.showMessageDialog(this, "관리자(admin) 계정은 삭제할 수 없습니다.", "삭제 불가", JOptionPane.ERROR_MESSAGE); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "정말로 '" + userIdToDelete + "' 사용자를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { users.removeIf(user -> user.getId().equals(userIdToDelete)); refreshUserTable(); }
        });
        return panel;
    }
    
    private JPanel createItemManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(new EmptyBorder(10, 10, 10, 10)); panel.setBackground(BG_LIGHT_GRAY);
        JPanel addItemPanel = new JPanel(new GridBagLayout());
        addItemPanel.setBorder(createTitledBorder("신규 물품 추가"));
        addItemPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5);
        JTextField itemNameField = new JTextField(10); JTextField itemStockField = new JTextField(5); JTextField itemFeeField = new JTextField(5);
        JButton addItemButton = new JButton("추가");
        addItemButton.setBackground(DK_BLUE); addItemButton.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; addItemPanel.add(new JLabel("물품명:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; addItemPanel.add(itemNameField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; addItemPanel.add(new JLabel("최대재고:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; addItemPanel.add(itemStockField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addItemPanel.add(new JLabel("기본요금:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; addItemPanel.add(itemFeeField, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; addItemPanel.add(addItemButton, gbc);
        panel.add(addItemPanel, BorderLayout.NORTH);

        String[] itemColumns = {"물품명", "현재/최대 재고", "기본 요금"};
        itemTableModel = new DefaultTableModel(itemColumns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; }};
        itemTable = new JTable(itemTableModel);
        styleTable(itemTable);
        refreshItemTable();
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(createTitledBorder("물품 목록"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton deleteItemButton = new JButton("선택한 물품 삭제");
        deleteItemButton.setBackground(new Color(225, 225, 225)); deleteItemButton.setForeground(Color.RED.darker()); deleteItemButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        panel.add(deleteItemButton, BorderLayout.SOUTH);
        
        addItemButton.addActionListener(e -> {
            try {
                String newItemName = itemNameField.getText().trim();
                if (newItemName.isEmpty()) { JOptionPane.showMessageDialog(this, "물품명을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE); return; }
                boolean nameExists = items.stream().anyMatch(item -> item.getName().equalsIgnoreCase(newItemName));
                if (nameExists) { JOptionPane.showMessageDialog(this, "이미 존재하는 물품 이름입니다.", "중복 오류", JOptionPane.ERROR_MESSAGE); return; }
                Item newItem = new Item(newItemName, Integer.parseInt(itemStockField.getText()), Double.parseDouble(itemFeeField.getText()));
                newItem.addObserver(parentFrame); items.add(newItem);
                JOptionPane.showMessageDialog(this, "물품이 추가되었습니다.");
                itemNameField.setText(""); itemStockField.setText(""); itemFeeField.setText("");
                refreshItemTable(); parentFrame.refreshItemComboBox();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "재고와 요금은 숫자로 입력해야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        deleteItemButton.addActionListener(e -> {
            int selectedRow = itemTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "삭제할 물품을 목록에서 선택해주세요.", "선택 오류", JOptionPane.WARNING_MESSAGE); return; }
            String itemNameToDelete = (String) itemTableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "정말로 '" + itemNameToDelete + "' 물품을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                items.removeIf(item -> item.getName().equals(itemNameToDelete));
                refreshItemTable(); parentFrame.refreshItemComboBox();
            }
        });
        return panel;
    }
    private void refreshUserTable() { userTableModel.setRowCount(0); for (User user : users) { userTableModel.addRow(new Object[]{user.getId(), user.getName(), user.getType()}); } }
    private void refreshItemTable() { itemTableModel.setRowCount(0); for (Item item : items) { itemTableModel.addRow(new Object[]{item.getName(), item.getCurrentStock() + "/" + item.getMaxStock(), String.format("%.0f", item.getBaseFee())}); } }
}