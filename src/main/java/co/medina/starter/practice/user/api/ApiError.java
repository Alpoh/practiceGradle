package co.medina.starter.practice.user.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public record ApiError(@JsonIgnore HttpStatus status, String message) {
}