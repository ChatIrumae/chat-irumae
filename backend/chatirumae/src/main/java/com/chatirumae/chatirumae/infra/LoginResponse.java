package com.chatirumae.chatirumae.infra;

public class LoginResponse {
    private String token;
    private String studentId;
    private String name;

    public LoginResponse() {}

    public LoginResponse(String token, String studentId, String name) {
        this.token = token;
        this.studentId = studentId;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}