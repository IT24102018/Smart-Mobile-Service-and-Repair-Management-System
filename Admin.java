package com.mobix.model;

public class Admin {
    private static final String ADMIN_EMAIL = "lakindunuwan2005@icloud.com";
    private static final String ADMIN_PASSWORD = "Admin123";
    
    private String email;
    private String password;
    
    public Admin() {}
    
    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public static boolean validateCredentials(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        String trimmedEmail = email.trim();
        String trimmedPassword = password.trim();
        return ADMIN_EMAIL.equals(trimmedEmail) && ADMIN_PASSWORD.equals(trimmedPassword);
    }
    
    public static String getAdminEmail() {
        return ADMIN_EMAIL;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
