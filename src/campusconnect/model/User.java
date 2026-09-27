package campusconnect.model;

public class User {

    private String username;
    private String password;   // plain text for now (this is a student project, not a bank)
    private String role;       // Admin, Coordinator, Volunteer

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean checkPassword(String input) {
        return this.password.equals(input);
    }

    // --- Getters and Setters ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
