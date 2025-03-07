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

import java.awt.geom.RectangularShape;
import java.net.http.HttpResponse;

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
            if (licenseActivationRequest.getActivationCode() == null) 
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите код активации!");

            if (licenseActivationRequest.getName() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите имя!");

            if (licenseActivationRequest.getMac_address() == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Введите MAC-Адрес!");

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