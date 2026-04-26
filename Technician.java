package com.mobix.model;

public class Technician {
    private static final String TECHNICIAN_EMAIL = "technician@gmail.com";
    private static final String TECHNICIAN_PASSWORD = "mobix123";

    private String email;
    private String password;

    public Technician() {
    }

    public Technician(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static boolean validateCredentials(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        String trimmedEmail = email.trim();
        String trimmedPassword = password.trim();
        return TECHNICIAN_EMAIL.equals(trimmedEmail) && TECHNICIAN_PASSWORD.equals(trimmedPassword);
    }

    public static String getTechnicianEmail() {
        return TECHNICIAN_EMAIL;
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
