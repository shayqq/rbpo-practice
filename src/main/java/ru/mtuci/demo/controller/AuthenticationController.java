package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.request.AuthenticationRequest;
import ru.mtuci.demo.model.AuthenticationResponse;
import ru.mtuci.demo.repository.UserRepository;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest,
                                   HttpServletRequest request, HttpServletResponse response) {
        try {

            String email = authenticationRequest.getEmail();

            ApplicationUser user = userRepository.findByEmail(email).orElseThrow(() ->
                    new UsernameNotFoundException("Пользователь не найден!"));

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email, authenticationRequest.getPassword()));

            String token = jwtTokenProvider.createToken(email, user.getRole().getGrantedAuthorities());

            return ResponseEntity.ok(new AuthenticationResponse(email, token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверная почта или пароль!");
        }
    }

}