package co.medina.starter.practice.auth;

public record RegisterRequest(String email, String password, String mobileNumber, String name, String address) {}

