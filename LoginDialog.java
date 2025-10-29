// 파일 이름: LoginDialog.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class LoginDialog extends JDialog {
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
        cancelButton.setBackground(Color.BLUE); cancelButton.setForeground(DK_BLUE); cancelButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
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