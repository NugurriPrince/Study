// 파일 이름: RentalSystem_Final.java
import javax.swing.*;
import java.util.List;

public class RentalSystem_Final {
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
}