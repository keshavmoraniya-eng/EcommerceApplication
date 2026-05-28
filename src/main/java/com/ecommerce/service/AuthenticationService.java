package com.ecommerce.service;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class AuthenticationService {
        // This service will handle authentication logic, such as validating user credentials,
        // generating JWT tokens, and managing refresh tokens. It will interact with the UserRepository
        // to retrieve user details and the RefreshTokenRepository to manage refresh tokens.

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public void register(RegisterRequest registerRequest){
        // Check if user already exists
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new IllegalArgumentException("Email is already in use");
        }

        User user=new User();
        user.setEmail(registerRequest.getEmail());
        user.setFullName(registerRequest.getFullName());
        user.setPhone(registerRequest.getPhone());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setIsVerified(false);

        // Assign default role (e.g., ROLE_USER)
        Role userRole=roleRepository.findByName("ROLE_USER")
                .orElseGet(()->{
                    Role r=new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });
        user.setRoles(Collections.singleton(userRole));
    }

    public AuthResponse login(String email, String password){
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        //if authentication is successful, generate JWT token and refresh token
        String accessToken=jwtUtil.generateToken(email);
        User user=userRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("User not found"));
        RefreshToken refreshToken=refreshTokenService.createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public String refreshAccessToken(String refreshTokenStr){
        RefreshToken refreshToken=refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(()-> new IllegalArgumentException("Invalid refresh token"));

        if(refreshTokenService.isExpired(refreshToken)){
            refreshTokenService.deleteByUser(refreshToken.getUser());
            throw new IllegalArgumentException("Refresh token has expired. Please login again.");
        }

        return jwtUtil.generateToken(refreshToken.getUser().getEmail());
    }

    public void logout(User user){
        refreshTokenService.deleteByUser(user);
    }

}
