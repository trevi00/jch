package org.jbd.backend.auth.service;

import org.jbd.backend.auth.dto.AuthenticationRequest;
import org.jbd.backend.auth.dto.AuthenticationResponse;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.dto.UserRegistrationDto;
import org.jbd.backend.user.dto.UserResponseDto;
import org.jbd.backend.user.repository.UserRepository;
import org.jbd.backend.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    public AuthenticationService(
            UserRepository userRepository,
            UserService userService,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    
    public AuthenticationResponse register(UserRegistrationDto request) {
        UserResponseDto userResponse = userService.registerUser(request);
        
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 활성 계정 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            userRepository.save(user);

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(UserResponseDto.from(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);

        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);

        } catch (LockedException e) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);

        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
    
    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            String userEmail = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmailAndIsDeletedFalse(userEmail)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .user(UserResponseDto.from(user))
                        .build();
            } else {
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
    
    public void logout(String token) {
        // JWT는 stateless하므로 별도의 로그아웃 처리가 필요하지 않음
        // 필요시 블랙리스트 구현 가능
    }
}