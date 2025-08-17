package com.moneython.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneython.userservice.dto.*;
import com.moneython.userservice.security.JwtTokenProvider;
import com.moneython.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application.yaml")
public class UserControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/user";
    }
    
    @Test
    public void testSignupSuccess() {
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("test@example.com")
                .password("password123")
                .nickname("testuser")
                .build();
        
        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                getBaseUrl() + "/signup", signupRequest, UserResponseDto.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getNickname()).isEqualTo("testuser");
        assertThat(response.getBody().getId()).isNotNull();
    }
    
    @Test
    public void testSignupEmailAlreadyExists() {
        // First signup
        SignupRequestDto signupRequest1 = SignupRequestDto.builder()
                .email("duplicate@example.com")
                .password("password123")
                .nickname("user1")
                .build();
        
        restTemplate.postForEntity(getBaseUrl() + "/signup", signupRequest1, UserResponseDto.class);
        
        // Second signup with same email
        SignupRequestDto signupRequest2 = SignupRequestDto.builder()
                .email("duplicate@example.com")
                .password("password456")
                .nickname("user2")
                .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/signup", signupRequest2, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void testLoginSuccess() {
        // First create a user
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("login@example.com")
                .password("password123")
                .nickname("loginuser")
                .build();
        
        restTemplate.postForEntity(getBaseUrl() + "/signup", signupRequest, UserResponseDto.class);
        
        // Then login
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email("login@example.com")
                .password("password123")
                .build();
        
        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                getBaseUrl() + "/login", loginRequest, LoginResponseDto.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isNotNull();
        assertThat(response.getBody().getTokenType()).isEqualTo("Bearer");
        assertThat(response.getBody().getUser().getEmail()).isEqualTo("login@example.com");
    }
    
    @Test
    public void testLoginInvalidCredentials() {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .email("nonexistent@example.com")
                .password("wrongpassword")
                .build();
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/login", loginRequest, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    public void testGetMyInfoSuccess() {
        // Create user and login
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("profile@example.com")
                .password("password123")
                .nickname("profileuser")
                .build();
        
        ResponseEntity<UserResponseDto> signupResponse = restTemplate.postForEntity(
                getBaseUrl() + "/signup", signupRequest, UserResponseDto.class);
        
        String token = jwtTokenProvider.generateToken("profile@example.com", signupResponse.getBody().getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                getBaseUrl() + "/me", HttpMethod.GET, entity, UserResponseDto.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("profile@example.com");
    }
    
    @Test
    public void testGetMyInfoUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity(getBaseUrl() + "/me", String.class);
        
        // Could be UNAUTHORIZED (401) or FORBIDDEN (403) depending on Spring Security configuration
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
    
    @Test
    public void testUpdateMyInfoSuccess() {
        // Create user
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("update@example.com")
                .password("password123")
                .nickname("oldnickname")
                .build();
        
        ResponseEntity<UserResponseDto> signupResponse = restTemplate.postForEntity(
                getBaseUrl() + "/signup", signupRequest, UserResponseDto.class);
        
        String token = jwtTokenProvider.generateToken("update@example.com", signupResponse.getBody().getId());
        
        // Update user
        UpdateUserRequestDto updateRequest = UpdateUserRequestDto.builder()
                .nickname("newnickname")
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<UpdateUserRequestDto> entity = new HttpEntity<>(updateRequest, headers);
        
        ResponseEntity<UserResponseDto> response = restTemplate.exchange(
                getBaseUrl() + "/me", HttpMethod.PUT, entity, UserResponseDto.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNickname()).isEqualTo("newnickname");
    }
    
    @Test
    public void testDeleteMyAccountSuccess() {
        // Create user
        SignupRequestDto signupRequest = SignupRequestDto.builder()
                .email("delete@example.com")
                .password("password123")
                .nickname("deleteuser")
                .build();
        
        ResponseEntity<UserResponseDto> signupResponse = restTemplate.postForEntity(
                getBaseUrl() + "/signup", signupRequest, UserResponseDto.class);
        
        String token = jwtTokenProvider.generateToken("delete@example.com", signupResponse.getBody().getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/me", HttpMethod.DELETE, entity, Void.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}