public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String regNumber;
    private String department;

    // Add constructor, getters, and setters
    public User(String name, String email, String password, String role, String regNumber, String department) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.regNumber = regNumber;
        this.department = department;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getRegNumber() { return regNumber; }
    public String getDepartment() { return department; }

    public void setId(String id) { this.id = id; }
}
