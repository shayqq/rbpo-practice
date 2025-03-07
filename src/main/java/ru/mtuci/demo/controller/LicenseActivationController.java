package ru.mtuci.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mtuci.demo.configuration.JwtTokenProvider;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.request.LicenseActivationRequest;
import ru.mtuci.demo.service.impl.DeviceServiceImpl;
import ru.mtuci.demo.service.impl.LicenseServiceImpl;
import ru.mtuci.demo.service.impl.UserDetailServiceImpl;

@RestController
@RequestMapping("/license")
@RequiredArgsConstructor
public class LicenseActivationController {

    private final DeviceServiceImpl deviceService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailServiceImpl userDetailsService;
    private final LicenseServiceImpl licenseService;

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody LicenseActivationRequest licenseActivationRequest,
                                      HttpServletRequest httpServletRequest) {
        try {
            String email = jwtTokenProvider.getUsername(httpServletRequest.getHeader("Authorization").substring(7));
            ApplicationUser user = userDetailsService.getUserByEmail(email).get();
            ApplicationDevice device = deviceService.registerOrUpdateDevice(licenseActivationRequest.getMac_address(),
                    licenseActivationRequest.getName(), user);

            ApplicationTicket ticket = licenseService.activateLicense(licenseActivationRequest.getActivationCode(), device, user);

            if (!ticket.getStatus().equals("OK")) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ticket.getInfo());

            return ResponseEntity.status(HttpStatus.OK).body(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Технические шоколадки...");
        }
    }

}