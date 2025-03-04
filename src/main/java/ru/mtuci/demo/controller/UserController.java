package ru.mtuci.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.service.impl.UserServiceImpl;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    //@PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/showAll")
    public ResponseEntity<?> showAll() {
        try {
            List<ApplicationUser> users = userService.getAll();
            return ResponseEntity.ok(users);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}