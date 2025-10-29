/* 


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// =================================================================================
// 1. 모델 클래스
// =================================================================================
class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id; private final String name; private final String type; private final String password;
    public User(String id, String name, String type, String password) { this.id = id; this.name = name; this.type = type; this.password = password; }
    public String getId() { return id; } public String getName() { return name; } public String getType() { return type; } public String getPassword() { return password; }
    @Override public String toString() { return name + " (" + type + ")"; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; User user = (User) o; return id.equals(user.id); }
    @Override public int hashCode() { return id.hashCode(); }
}

class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name; private final int maxStock; private List<User> renters; private final double baseFee;
    private transient List<Observer> observers = new ArrayList<>();
    public Item(String name, int initialStock, double baseFee) { this.name = name; this.maxStock = initialStock; this.baseFee = baseFee; this.renters = new ArrayList<>(); }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { in.defaultReadObject(); this.observers = new ArrayList<>(); if (this.renters == null) { this.renters = new ArrayList<>(); } }
    private List<Observer> getObservers() { if (observers == null) { observers = new ArrayList<>(); } return observers; }
    public String getName() { return name; }
    public int getMaxStock() { return maxStock; }
    public int getCurrentStock() { return maxStock - renters.size(); }
    public double getBaseFee() { return baseFee; }
    public boolean rentTo(User user) { if (getCurrentStock() > 0) { renters.add(user); notifyObservers(); return true; } return false; }
    public boolean returnBy(User user) { if (renters.contains(user)) { renters.remove(user); notifyObservers(); return true; } return false; }
    public void addObserver(Observer observer) { getObservers().add(observer); }
    public void notifyObservers() { getObservers().forEach(observer -> observer.update(this)); }
    @Override public String toString() { return name; }
}

class RentalRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final User user; private final String itemName; private final LocalDateTime rentalTime; private LocalDateTime returnTime;
    public RentalRecord(User user, String itemName) { this.user = user; this.itemName = itemName; this.rentalTime = LocalDateTime.now(); this.returnTime = null; }
    public void markAsReturned() { this.returnTime = LocalDateTime.now(); }
    public User getUser() { return user; } public String getItemName() { return itemName; } public LocalDateTime getRentalTime() { return rentalTime; } public LocalDateTime getReturnTime() { return returnTime; }
}

// =================================================================================
// 2. 디자인 패턴 & 비즈니스 로직
// =================================================================================
interface Observer { void update(Item item); }
interface DiscountStrategy { double applyDiscount(double originalFee); String getStrategyName(); }
class StudentDiscountStrategy implements DiscountStrategy { @Override public double applyDiscount(double originalFee) { return originalFee * 0.8; } @Override public String getStrategyName() { return "학생 할인 (20%)"; } }
class NoDiscountStrategy implements DiscountStrategy { @Override public double applyDiscount(double originalFee) { return originalFee; } @Override public String getStrategyName() { return "일반 (할인 없음)"; } }

class RentalService {
    private final List<RentalRecord> rentalHistory;
    public RentalService(List<RentalRecord> rentalHistory) { this.rentalHistory = rentalHistory; }
    public String rentItem(User user, Item item, DiscountStrategy strategy) { if (item.rentTo(user)) { rentalHistory.add(new RentalRecord(user, item.getName())); return String.format("[대여 성공] %s -> %s", user.getName(), item.getName()); } else { return "[대여 실패] " + item.getName() + " 재고가 없습니다."; } }
    public String returnItem(User user, Item item) { Optional<RentalRecord> activeRecord = rentalHistory.stream().filter(r -> r.getUser().equals(user) && r.getItemName().equals(item.getName()) && r.getReturnTime() == null).findFirst(); if (activeRecord.isPresent()) { if (item.returnBy(user)) { activeRecord.get().markAsReturned(); return String.format("[반납 성공] %s <- %s", user.getName(), item.getName()); } } return String.format("[반납 실패] %s님은 %s을(를) 대여하지 않았습니다.", user.getName(), item.getName()); }
}

// =================================================================================
// 3. 데이터 관리 클래스
// =================================================================================
class DataManager {
    private static final String USERS_FILE = "users.dat"; private static final String ITEMS_FILE = "items.dat"; private static final String HISTORY_FILE = "history.dat";
    public void saveData(List<User> users, List<Item> items, List<RentalRecord> history) { try (ObjectOutputStream oosUsers = new ObjectOutputStream(new FileOutputStream(USERS_FILE)); ObjectOutputStream oosItems = new ObjectOutputStream(new FileOutputStream(ITEMS_FILE)); ObjectOutputStream oosHistory = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) { oosUsers.writeObject(users); oosItems.writeObject(items); oosHistory.writeObject(history); System.out.println("모든 데이터가 성공적으로 저장되었습니다."); } catch (IOException e) { System.err.println("데이터 저장 중 오류 발생: " + e.getMessage()); } }
    @SuppressWarnings("unchecked") public List<User> loadUsers() { File file = new File(USERS_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<User>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("사용자 데이터 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
    @SuppressWarnings("unchecked") public List<Item> loadItems() { File file = new File(ITEMS_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<Item>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("물품 데이터 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
    @SuppressWarnings("unchecked") public List<RentalRecord> loadHistory() { File file = new File(HISTORY_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<RentalRecord>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("대여 기록 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
}

// =================================================================================
// 4. UI 구현부
// =================================================================================
class MainAppFrame extends JFrame implements Observer {
    private final DataManager dataManager; private final RentalService rentalService; private final User loggedInUser;
    private final List<Item> items; private final List<User> users; private final List<RentalRecord> rentalHistory;
    private JComboBox<Item> itemComboBox; private JLabel strategyLabel;
    private JPanel itemListPanel;
    private JTextArea logArea; private DiscountStrategy userStrategy;
    private static final Color DK_BLUE = new Color(0, 44, 122); private static final Color BG_LIGHT_GRAY = new Color(245, 245, 245);

    public MainAppFrame(DataManager dataManager, RentalService rentalService, User loggedInUser, List<User> users, List<Item> items, List<RentalRecord> rentalHistory) {
        this.dataManager = dataManager; this.rentalService = rentalService; this.loggedInUser = loggedInUser;
        this.users = users; this.items = items; this.rentalHistory = rentalHistory;
        items.forEach(item -> item.addObserver(this));
        if ("Student".equals(loggedInUser.getType())) { this.userStrategy = new StudentDiscountStrategy(); } else { this.userStrategy = new NoDiscountStrategy(); }
        setupUI();
        updateItemDisplay();
    }
    
    private void setupUI() {
        setTitle("단국대학교 물품 대여 시스템"); setSize(900, 1000);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dataManager.saveData(users, items, rentalHistory); System.exit(0); } });
        getContentPane().setBackground(BG_LIGHT_GRAY);
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JMenu systemMenu = new JMenu("시스템");
        systemMenu.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        JMenuItem logoutItem = new JMenuItem("로그아웃");
        JMenuItem exitItem = new JMenuItem("종료");
        logoutItem.addActionListener(e -> { dataManager.saveData(users, items, rentalHistory); dispose(); RentalSystem_Final.main(null); });
        exitItem.addActionListener(e -> { dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); });
        systemMenu.add(logoutItem); systemMenu.add(exitItem); menuBar.add(systemMenu);
        if ("Admin".equals(loggedInUser.getType())) {
            JMenu adminMenu = new JMenu("관리 (Admin)");
            adminMenu.setForeground(DK_BLUE);
            adminMenu.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            JMenuItem manageUsersItems = new JMenuItem("사용자/물품 관리...");
            manageUsersItems.addActionListener(e -> new AdminDialog(this, users, items).setVisible(true));
            JMenuItem viewHistory = new JMenuItem("전체 대여 기록 보기...");
            viewHistory.addActionListener(e -> new RentalHistoryDialog(this, rentalHistory).setVisible(true));
            adminMenu.add(manageUsersItems); adminMenu.add(viewHistory); menuBar.add(adminMenu);
        }
        setJMenuBar(menuBar);
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BG_LIGHT_GRAY);
        add(mainPanel, BorderLayout.CENTER);
        JPanel headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setBackground(DK_BLUE);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        try {
            ImageIcon dankookLogo = new ImageIcon("단국대 로고.png");
            Image image = dankookLogo.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            headerPanel.add(imageLabel, BorderLayout.WEST);
        } catch (Exception e) { System.err.println("메인 로고 이미지 로딩 실패. 이미지를 생략합니다."); }
        JLabel titleLabel = new JLabel("물품 대여 시스템");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        JPanel controlPanel = createControlPanel();
        JSplitPane splitPane = createInfoPanel();
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(controlPanel, BorderLayout.NORTH); centerPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
    private TitledBorder createTitledBorder(String title) { TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(DK_BLUE), title); border.setTitleColor(DK_BLUE); border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 14)); return border; }
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createCompoundBorder(createTitledBorder("대여 / 반납"), new EmptyBorder(5, 5, 5, 5))); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL; JLabel userLabel = new JLabel("<html><b>" + loggedInUser.getName() + "</b> 님, 환영합니다.</html>"); userLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        itemComboBox = new JComboBox<>(items.toArray(new Item[0]));
        itemComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        itemComboBox.setRenderer(new ItemRenderer());
        strategyLabel = new JLabel("적용 정책: " + userStrategy.getStrategyName()); strategyLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JButton rentButton = new JButton("대여하기");
        JButton returnButton = new JButton("반납하기");
        Font btnFont = new Font("맑은 고딕", Font.BOLD, 14);
        rentButton.setBackground(DK_BLUE); rentButton.setForeground(Color.BLACK); rentButton.setFont(btnFont);
        returnButton.setBackground(Color.WHITE); returnButton.setForeground(DK_BLUE); returnButton.setFont(btnFont); rentButton.setBorder(BorderFactory.createLineBorder(DK_BLUE, 2)); returnButton.setBorder(BorderFactory.createLineBorder(DK_BLUE, 2)); rentButton.addActionListener(e -> rentAction()); returnButton.addActionListener(e -> returnAction()); gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; panel.add(userLabel, gbc); gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("물품 선택:"), gbc); gbc.gridx = 1; gbc.weightx = 1.0; panel.add(itemComboBox, gbc); gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST; panel.add(strategyLabel, gbc); JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); buttonPanel.setBackground(Color.WHITE); buttonPanel.add(rentButton); buttonPanel.add(returnButton); gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST; panel.add(buttonPanel, gbc); return panel;
    }
    private JSplitPane createInfoPanel() {
        JPanel itemDisplayPanel = new JPanel(new BorderLayout());
        itemDisplayPanel.setBackground(Color.WHITE);
        itemDisplayPanel.setBorder(createTitledBorder("실시간 물품 재고"));
        itemListPanel = new JPanel();
        itemListPanel.setLayout(new GridLayout(0, 1, 0, 0)); // 간격 제거
        itemListPanel.setBackground(Color.WHITE);
        itemListPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(itemListPanel, BorderLayout.NORTH);
        itemDisplayPanel.add(new JScrollPane(wrapperPanel), BorderLayout.CENTER);
        JPanel logPanel = new JPanel(new BorderLayout()); logPanel.setBackground(Color.WHITE); logPanel.setBorder(createTitledBorder("실시간 활동 기록"));
        logArea = new JTextArea(); logArea.setEditable(false); logArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); logArea.setBorder(new EmptyBorder(5, 10, 5, 10));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, itemDisplayPanel, logPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        return splitPane;
    }

    private JPanel createItemCardPanel(Item item) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(10, 5, 10, 5)); // 내부 여백

        // --- UI 개선: 점선 테두리 ---
        Border dashedBorder = BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 1, 3);
        card.setBorder(BorderFactory.createCompoundBorder(dashedBorder, new EmptyBorder(8, 8, 8, 8)));

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        card.add(nameLabel, BorderLayout.CENTER); // 중앙 정렬로 변경

        int currentStock = item.getCurrentStock();
        JLabel stockLabel = new JLabel("재고: " + currentStock + "개");
        
        // --- UI 개선: 폰트 굵게 및 크기 조절 ---
        stockLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15)); 

        if (currentStock == 0) {
            stockLabel.setForeground(Color.RED);
        } else if (currentStock <= 5) {
            stockLabel.setForeground(new Color(255, 128, 0));
        } else {
            stockLabel.setForeground(DK_BLUE);
        }
        card.add(stockLabel, BorderLayout.EAST);
        
        return card;
    }
    
    private void updateItemDisplay() {
        itemListPanel.removeAll();
        for (Item item : items) {
            itemListPanel.add(createItemCardPanel(item));
        }
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }
    
    @Override public void update(Item item) {
        updateItemDisplay();
    }
    private void rentAction() { Item selectedItem = (Item) itemComboBox.getSelectedItem(); if (selectedItem != null) { String result = rentalService.rentItem(loggedInUser, selectedItem, userStrategy); logArea.append(result + "\n"); } }
    private void returnAction() { Item selectedItem = (Item) itemComboBox.getSelectedItem(); if (selectedItem != null) { String result = rentalService.returnItem(loggedInUser, selectedItem); logArea.append(result + "\n"); } }
    public void refreshItemComboBox() { DefaultComboBoxModel<Item> model = (DefaultComboBoxModel<Item>) itemComboBox.getModel(); model.removeAllElements(); items.forEach(model::addElement); updateItemDisplay(); }
    
    class ItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Item) {
                Item item = (Item) value;
                if (item.getCurrentStock() > 0) {
                    setText(item.getName());
                    setForeground(Color.BLACK);
                } else {
                    setText(item.getName() + " (대여 불가)");
                    setForeground(Color.GRAY);
                }
            }
            return this;
        }
    }
}

class LoginDialog extends JDialog {
    private User loggedInUser = null;
    private static final Color DK_BLUE = new Color(0, 44, 122);
    private static final Color BG_LIGHT_GRAY = new Color(245, 245, 245);
    public LoginDialog(Frame parent, List<User> users) {
        super(parent, "시스템 로그인", true);
        setUndecorated(true); setSize(400, 500); setLayout(new BorderLayout());
        getRootPane().setBorder(BorderFactory.createLineBorder(DK_BLUE, 2));
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        headerPanel.setBackground(DK_BLUE);
        try {
            ImageIcon dankookLogo = new ImageIcon("단국대 로고.png");
            Image image = dankookLogo.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            headerPanel.add(imageLabel);
        } catch (Exception e) { System.err.println("로그인 로고 이미지 로딩 실패. 이미지를 생략합니다."); }
        JLabel titleLabel = new JLabel("물품 대여 시스템");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BG_LIGHT_GRAY);
        inputPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 5, 10, 5);
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14); Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 14);
        JLabel idLabel = new JLabel("사용자 ID "); idLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0; inputPanel.add(idLabel, gbc);
        JTextField idField = new JTextField(15); idField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = 0; inputPanel.add(idField, gbc);
        JLabel passwordLabel = new JLabel("비밀번호 "); passwordLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1; inputPanel.add(passwordLabel, gbc);
        JPasswordField passwordField = new JPasswordField(15); passwordField.setFont(fieldFont);
        gbc.gridx = 1; gbc.gridy = 1; inputPanel.add(passwordField, gbc);
        add(inputPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(BG_LIGHT_GRAY);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JButton loginButton = new JButton("로그인");
        JButton cancelButton = new JButton("종료");
        Dimension btnSize = new Dimension(150, 40);
        loginButton.setPreferredSize(btnSize); cancelButton.setPreferredSize(btnSize);
        loginButton.setBackground(DK_BLUE); loginButton.setForeground(Color.RED); loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        cancelButton.setBackground(Color.WHITE); cancelButton.setForeground(DK_BLUE); cancelButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        buttonPanel.add(loginButton); buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> {
            Optional<User> user = users.stream().filter(u -> u.getId().equals(idField.getText()) && u.getPassword().equals(new String(passwordField.getPassword()))).findFirst();
            if (user.isPresent()) { this.loggedInUser = user.get(); dispose(); }
            else { JOptionPane.showMessageDialog(this, "ID 또는 비밀번호가 잘못되었습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE); }
        });
        cancelButton.addActionListener(e -> { this.loggedInUser = null; dispose(); });
        setLocationRelativeTo(parent);
    }
    public User getLoggedInUser() { return this.loggedInUser; }
}

class AdminDialog extends JDialog {
    private MainAppFrame parentFrame; private List<User> users; private List<Item> items;
    private JTable userTable; private JTable itemTable;
    private DefaultTableModel userTableModel; private DefaultTableModel itemTableModel;
    public AdminDialog(MainAppFrame parent, List<User> users, List<Item> items) {
        super(parent, "관리자 패널", true);
        this.parentFrame = parent; this.users = users; this.items = items;
        setSize(800, 600); setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("사용자 관리", createUserManagementPanel());
        tabbedPane.addTab("물품 관리", createItemManagementPanel());
        add(tabbedPane, BorderLayout.CENTER); setLocationRelativeTo(parent);
    }
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel addUserPanel = new JPanel(new GridBagLayout()); addUserPanel.setBorder(new TitledBorder("신규 사용자 추가"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5);
        JTextField userIdField = new JTextField(10); JTextField userNameField = new JTextField(10); JTextField userPassField = new JTextField(10);
        JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"Student", "Staff", "Admin"}); JButton addUserButton = new JButton("사용자 추가");
        gbc.gridx = 0; gbc.gridy = 0; addUserPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; addUserPanel.add(userIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addUserPanel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; addUserPanel.add(userNameField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; addUserPanel.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; addUserPanel.add(userPassField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; addUserPanel.add(new JLabel("타입:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; addUserPanel.add(userTypeCombo, gbc);
        gbc.gridx = 4; gbc.gridy = 1; addUserPanel.add(addUserButton, gbc);
        panel.add(addUserPanel, BorderLayout.NORTH);
        String[] userColumns = {"ID", "이름", "타입"};
        userTableModel = new DefaultTableModel(userColumns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; }};
        userTable = new JTable(userTableModel); refreshUserTable();
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JButton deleteUserButton = new JButton("선택한 사용자 삭제"); panel.add(deleteUserButton, BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel addItemPanel = new JPanel(new GridBagLayout()); addItemPanel.setBorder(new TitledBorder("신규 물품 추가"));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 5);
        JTextField itemNameField = new JTextField(10); JTextField itemStockField = new JTextField(5); JTextField itemFeeField = new JTextField(5);
        JButton addItemButton = new JButton("물품 추가");
        gbc.gridx = 0; gbc.gridy = 0; addItemPanel.add(new JLabel("물품명:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; addItemPanel.add(itemNameField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; addItemPanel.add(new JLabel("최대재고:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; addItemPanel.add(itemStockField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; addItemPanel.add(new JLabel("기본요금:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; addItemPanel.add(itemFeeField, gbc);
        gbc.gridx = 3; gbc.gridy = 1; addItemPanel.add(addItemButton, gbc);
        panel.add(addItemPanel, BorderLayout.NORTH);
        String[] itemColumns = {"물품명", "현재/최대 재고", "기본 요금"};
        itemTableModel = new DefaultTableModel(itemColumns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; }};
        itemTable = new JTable(itemTableModel); refreshItemTable();
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        JButton deleteItemButton = new JButton("선택한 물품 삭제"); panel.add(deleteItemButton, BorderLayout.SOUTH);
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

class RentalHistoryDialog extends JDialog {
    public RentalHistoryDialog(Frame parent, List<RentalRecord> history) {
        super(parent, "전체 대여 기록", true); setSize(700, 500);
        String[] columnNames = {"사용자", "물품명", "대여시간", "반납시간"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        JTable table = new JTable(tableModel);
        table.setForeground(Color.BLACK); table.setBackground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0, 44, 122)); header.setForeground(Color.BLACK); header.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        table.setRowHeight(24); table.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (RentalRecord record : history) {
            String userName = record.getUser().getName(); String itemName = record.getItemName();
            String rentalTime = record.getRentalTime().format(formatter);
            String returnTime = (record.getReturnTime() != null) ? record.getReturnTime().format(formatter) : " (대여 중)";
            tableModel.addRow(new Object[]{userName, itemName, rentalTime, returnTime});
        }
        add(new JScrollPane(table));
        setLocationRelativeTo(parent);
    }
}

// =================================================================================
// 5. 메인 실행 클래스
// =================================================================================
public class RentalSystem_Original {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); }
        DataManager dataManager = new DataManager();
        List<User> users = dataManager.loadUsers();
        List<Item> items = dataManager.loadItems();
        List<RentalRecord> rentalHistory = dataManager.loadHistory();
        if (users.isEmpty()) {
            users.add(new User("admin", "관리자", "Admin", "admin123"));
            users.add(new User("student1", "김민준", "Student", "1234"));
            users.add(new User("staff1", "박선우", "Staff", "abcd"));
        }
        if (items.isEmpty()) {
            items.add(new Item("3단 우산", 10, 1000.0));
            items.add(new Item("축구공", 5, 2000.0));
            items.add(new Item("보조배터리", 15, 1500.0));
            items.add(new Item("C타입 충전기", 20, 500.0));
            items.add(new Item("8핀 충전기", 20, 500.0));
        }
        RentalService rentalService = new RentalService(rentalHistory);
        LoginDialog loginDialogInstance = new LoginDialog(null, users);
        loginDialogInstance.setVisible(true);
        User loggedInUser = loginDialogInstance.getLoggedInUser();
        if (loggedInUser != null) {
            SwingUtilities.invokeLater(() -> {
                MainAppFrame mainFrame = new MainAppFrame(dataManager, rentalService, loggedInUser, users, items, rentalHistory);
                mainFrame.setVisible(true);
            });
        } else {
             System.out.println("로그인하지 않거나 종료를 선택하여 프로그램을 종료합니다.");
        }
    }

*/