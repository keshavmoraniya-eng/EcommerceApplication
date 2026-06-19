package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.RefreshToken;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication",description = "Authentication and Authorization endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationService authenticationService,UserRepository userRepository){
        this.authenticationService=authenticationService;
        this.userRepository=userRepository;
    }


    @PostMapping("/register")
    @Operation(summary = "Register a new user",description = "Register a new user with email and password")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request){
        try {
            authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true,"User register successfully","Registration completed"));
        }catch (IllegalArgumentException exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false,exception.getMessage(),null));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false,"Registration failed: "+e.getMessage(),null));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user",description = "Login a user with email and password and return access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request){
        AuthResponse tokens=authenticationService.login(request.getEmail(),request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(true,"Login successful",tokens));
    }


    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",description = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request){
        try {
            AuthResponse tokens=authenticationService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(new ApiResponse<>(true,"Token refreshed successfully",tokens));
        }catch (IllegalArgumentException exception){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false,exception.getMessage(),null));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false,"Token refresh failed: "+e.getMessage(),null));
        }
    }


    @PostMapping("/logout")
    @Operation(summary = "Logout a user",description = "Logout a user by invalidating the refresh token",security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal UserDetails userDetails){
        User user=userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(()-> new IllegalArgumentException("User not found with email: "+userDetails.getUsername()));
        authenticationService.logout(user);
        return ResponseEntity.ok(new ApiResponse<>(true,"Logout successful","User logged out successfully"));

    }

}
