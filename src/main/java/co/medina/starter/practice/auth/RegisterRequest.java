package co.medina.starter.practice.auth;

import lombok.Builder;

@Builder
public record RegisterRequest(String email, String password, String mobileNumber, String name, String address) {}

