package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.ApplicationRole;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.UserRepository;
import ru.mtuci.demo.request.RegistrationRequest;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationRequest registrationRequest) {
        try {
            if (registrationRequest.getUsername() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите логин");

            if (registrationRequest.getPassword() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите пароль");

            if (registrationRequest.getEmail() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите почту");

            if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь с таким логином уже существует!");

            if (userRepository.findByEmail(registrationRequest.getEmail()).isPresent())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Данная почта уже используется!");

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            ApplicationUser newUser = new ApplicationUser();
            newUser.setUsername(registrationRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            newUser.setEmail(registrationRequest.getEmail());
            newUser.setRole(ApplicationRole.USER);

            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.OK).body("Регистрация успешно произведена!");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

}