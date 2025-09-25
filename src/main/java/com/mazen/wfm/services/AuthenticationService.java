package com.mazen.wfm.services;

import com.mazen.wfm.dto.auth.AuthenticationRequest;
import com.mazen.wfm.dto.auth.AuthenticationResponse;
import com.mazen.wfm.dto.auth.RefreshTokenRequest;
import com.mazen.wfm.dto.auth.RegisterRequest;
import com.mazen.wfm.models.AppUser;
import com.mazen.wfm.models.UserRole;
import com.mazen.wfm.repositories.AppUserRepository;
import com.mazen.wfm.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthenticationResponse register(RegisterRequest request, UserRole role) {
        // Check if user already exists
        if (userService.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        var user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(role)
                .active(true)
                .build();

        userService.save(user);

        // Generate tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthenticationResponse(user, jwtToken, refreshToken);
    }
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        var user = userService.findByUsername(request.username());

//        // Verify the user is a team leader
//        if (user.getRole() != UserRole.TEAM_LEADER) {
//            throw new RuntimeException("Only team leaders are allowed to log in");
//        }
        // Verify the user is active
        if (!user.getActive()) {
            throw new RuntimeException("User account is deactivated");
        }
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthenticationResponse(user, jwtToken, refreshToken);
    }
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = userService.findByUsername(username);
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                return buildAuthenticationResponse(user, accessToken, newRefreshToken);
            }
        }

        throw new RuntimeException("Invalid refresh token");
    }
    private AuthenticationResponse buildAuthenticationResponse(AppUser user, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // Convert to seconds
                .user(AuthenticationResponse.UserInfo.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.getActive())
                        .build())
                .build();
    }
}
