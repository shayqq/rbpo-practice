package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.request.LicenseCreateRequest;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;
import ru.mtuci.demo.service.impl.LicenseTypeServiceImpl;
import ru.mtuci.demo.service.impl.ProductServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailServiceImpl;

@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class LicenseCreateController {

    private final ProductServiceImpl productService;
    private final UserDetailServiceImpl userService;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final LicenseServiceImpl licenseService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailServiceImpl userDetailsService;

    @PostMapping("/createadm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseCreateRequest request, HttpServletRequest req) {
        try {
            Long productId = request.getProductId();
            Long ownerId = request.getOwnerId();
            Long licenseTypeId = request.getLicenseTypeId();

            if (productService.getProductById(productId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Продукт не найден!");
            }

            if (productService.getProductById(productId).get().isBlocked()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Продукт не доступен!");
            }

            if (userService.getUserById(ownerId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Владелец не найден!");
            }

            if (licenseTypeService.getLicenseTypeById(licenseTypeId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Тип лицензии не найден!");
            }

            String email = jwtTokenProvider.getUsername(req.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();

            Long id = licenseService.createLicense(productId, ownerId, licenseTypeId, user, request.getCount());

            return ResponseEntity.status(HttpStatus.OK).body("Лицензия успешно создана\nID: " + id);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Технические шоколадки...");
        }
    }

}