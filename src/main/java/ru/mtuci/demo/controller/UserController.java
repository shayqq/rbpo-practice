package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.demo.configuration.SecurityConfig;
import ru.mtuci.demo.model.ApplicationRole;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.repository.UserRepository;
import ru.mtuci.demo.request.RegistrationRequest;
import ru.mtuci.demo.request.RequestUser;
import ru.mtuci.demo.service.impl.UserServiceImpl;
import java.util.List;
import java.util.Optional;

@RestController("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/showAlladm")
    public ResponseEntity<?> showAlladm() {
        try {
            List<ApplicationUser> users = userService.getAll();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/showAll")
    public ResponseEntity<?> showAll() {
        try {
            List<ApplicationUser> users = userService.getAll();
            List<RequestUser> data = users.stream().map(
                    user -> new RequestUser(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole()
                    )
            ).toList();
            return ResponseEntity.status(HttpStatus.OK).body(data);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createadm")
    public ResponseEntity<?> createadm(@RequestBody RegistrationRequest registrationRequest) {
        try {
            String username = registrationRequest.getUsername();
            String email = registrationRequest.getEmail();

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
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            newUser.setEmail(email);
            newUser.setRole(ApplicationRole.USER);

            userRepository.save(newUser);

            return ResponseEntity.status(HttpStatus.OK).body("Пользователь создан!");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateadm")
    public ResponseEntity<?> updateadm(@RequestBody ApplicationUser applicationUser) {
        try {
            if (applicationUser.getId() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите id пользователя!");
            Optional<ApplicationUser> userOptional = userRepository.findById(applicationUser.getId());
            if (!userOptional.isPresent())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден!");
            ApplicationUser user = userOptional.get();
            if (applicationUser.getUsername() == null) applicationUser.setUsername(user.getUsername());
            if (applicationUser.getPassword() == null) applicationUser.setPassword(user.getPassword());
            else applicationUser.setPassword(securityConfig.passwordEncoder().encode(applicationUser.getPassword()));
            if (applicationUser.getEmail() == null) applicationUser.setEmail(user.getEmail());
            if (applicationUser.getRole() == null) applicationUser.setRole(user.getRole());
            userRepository.save(applicationUser);
            return ResponseEntity.status(HttpStatus.OK).body("Пользователь успешно изменен!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteadm")
    public ResponseEntity<?> deleteadm(@RequestBody ApplicationUser applicationUser) {
        try {
            if (applicationUser.getId() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите id пользователя!");
            Optional<ApplicationUser> userOptional = userRepository.findById(applicationUser.getId());
            if (!userOptional.isPresent())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден!");
            userRepository.delete(userOptional.get());
            return ResponseEntity.status(HttpStatus.OK).body("Пользователь успешно удален!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

}