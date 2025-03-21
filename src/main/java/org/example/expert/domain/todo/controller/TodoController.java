package org.example.expert.domain.todo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.expert.config.CustomUserDetails;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/todos")
    public ResponseEntity<TodoSaveResponse> saveTodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TodoSaveRequest todoSaveRequest

    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("🔍 현재 인증 정보: " + authentication);
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("❌ 인증된 사용자가 없습니다. SecurityContext가 비어 있음.");
        }

        Object principal = authentication.getPrincipal();
        System.out.println("🔍 principal: " + principal);

        if (principal instanceof CustomUserDetails customUserDetails) {
            AuthUser authUser = customUserDetails.toAuthUser();
            return ResponseEntity.ok(todoService.saveTodo(authUser, todoSaveRequest));
        }

        throw new RuntimeException("❌ 인증된 사용자가 아닙니다.");
    }

    @GetMapping("/todos")
    public ResponseEntity<Page<TodoResponse>> getTodos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(todoService.getTodos(page, size));
    }

    @GetMapping("/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(@PathVariable long todoId) {
        return ResponseEntity.ok(todoService.getTodo(todoId));
    }

    @GetMapping("/todos/search")
    public Page<TodoResponse> searchTodos(
            @RequestParam(required = false) String weather,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        return todoService.searchTodos(weather, startDate, endDate, page, size);
    }

}
