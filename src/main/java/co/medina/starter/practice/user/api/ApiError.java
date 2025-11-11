package co.medina.starter.practice.user.api;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record ApiError(@JsonIgnore HttpStatus status, String message) {
}