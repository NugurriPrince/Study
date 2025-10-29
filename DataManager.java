// 파일 이름: DataManager.java
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String USERS_FILE = "users.dat"; private static final String ITEMS_FILE = "items.dat"; private static final String HISTORY_FILE = "history.dat";
    public void saveData(List<User> users, List<Item> items, List<RentalRecord> history) { try (ObjectOutputStream oosUsers = new ObjectOutputStream(new FileOutputStream(USERS_FILE)); ObjectOutputStream oosItems = new ObjectOutputStream(new FileOutputStream(ITEMS_FILE)); ObjectOutputStream oosHistory = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) { oosUsers.writeObject(users); oosItems.writeObject(items); oosHistory.writeObject(history); System.out.println("모든 데이터가 성공적으로 저장되었습니다."); } catch (IOException e) { System.err.println("데이터 저장 중 오류 발생: " + e.getMessage()); } }
    @SuppressWarnings("unchecked") public List<User> loadUsers() { File file = new File(USERS_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<User>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("사용자 데이터 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
    @SuppressWarnings("unchecked") public List<Item> loadItems() { File file = new File(ITEMS_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<Item>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("물품 데이터 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
    @SuppressWarnings("unchecked") public List<RentalRecord> loadHistory() { File file = new File(HISTORY_FILE); if (file.exists()) { try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { return (List<RentalRecord>) ois.readObject(); } catch (IOException | ClassNotFoundException e) { System.err.println("대여 기록 로딩 중 오류 발생: " + e.getMessage()); } } return new ArrayList<>(); }
}