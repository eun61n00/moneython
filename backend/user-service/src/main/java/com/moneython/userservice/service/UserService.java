package com.moneython.userservice.service;

import com.moneython.userservice.dto.*;
import com.moneython.userservice.entity.User;
import com.moneython.userservice.repository.UserRepository;
import com.moneython.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public UserResponseDto signup(SignupRequestDto signupRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        
        // Check if nickname already exists
        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        
        // Create new user
        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .nickname(signupRequest.getNickname())
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }
    
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다."));
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
        
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        return new LoginResponseDto(token, convertToResponseDto(user));
    }
    
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return convertToResponseDto(user);
    }
    
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // Update nickname if provided
        if (updateRequest.getNickname() != null && !updateRequest.getNickname().trim().isEmpty()) {
            if (!user.getNickname().equals(updateRequest.getNickname()) && 
                userRepository.existsByNickname(updateRequest.getNickname())) {
                throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
            }
            user.setNickname(updateRequest.getNickname());
        }
        
        // Update password if provided
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }
        
        User updatedUser = userRepository.save(user);
        return convertToResponseDto(updatedUser);
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
    
    private UserResponseDto convertToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}