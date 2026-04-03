package com.notifyflow.notification.controller;

import com.notifyflow.notification.model.dto.NotificationRequest;
import com.notifyflow.notification.model.dto.NotificationResponse;
import com.notifyflow.notification.model.enums.NotificationStatus;
import com.notifyflow.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification dispatch and tracking")
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    @Operation(summary = "Dispatch a new notification")
    public ResponseEntity<NotificationResponse> dispatch(
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dispatch(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all notifications for a user")
    public ResponseEntity<Page<NotificationResponse>> getByUser(
            @PathVariable String userId, Pageable pageable) {
        return ResponseEntity.ok(service.getByUser(userId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get notifications by status")
    public ResponseEntity<Page<NotificationResponse>> getByStatus(
            @PathVariable NotificationStatus status, Pageable pageable) {
        return ResponseEntity.ok(service.getByStatus(status, pageable));
    }
}
