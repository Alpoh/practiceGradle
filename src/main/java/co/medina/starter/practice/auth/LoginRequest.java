package co.medina.starter.practice.auth;

import lombok.Builder;

@Builder
public record LoginRequest(String email, String password) {}

