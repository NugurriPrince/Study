// 파일 이름: User.java
import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id; private final String name; private final String type; private final String password;
    public User(String id, String name, String type, String password) { this.id = id; this.name = name; this.type = type; this.password = password; }
    public String getId() { return id; } public String getName() { return name; } public String getType() { return type; } public String getPassword() { return password; }
    @Override public String toString() { return name + " (" + type + ")"; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; User user = (User) o; return id.equals(user.id); }
    @Override public int hashCode() { return id.hashCode(); }
}