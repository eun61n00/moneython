package com.moneython.userservice.controller;

import com.moneython.userservice.dto.*;
import com.moneython.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody SignupRequestDto signupRequest) {
        UserResponseDto userResponse = userService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = userService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
    
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    public ResponseEntity<UserResponseDto> getMyInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserResponseDto userResponse = userService.getUserById(userId);
        return ResponseEntity.ok(userResponse);
    }
    
    @PutMapping("/me")
    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @Valid @RequestBody UpdateUserRequestDto updateRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UserResponseDto userResponse = userService.updateUser(userId, updateRequest);
        return ResponseEntity.ok(userResponse);
    }
    
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 삭제합니다.")
    public ResponseEntity<Void> deleteMyAccount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}