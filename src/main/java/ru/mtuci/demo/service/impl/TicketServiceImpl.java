package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;
import ru.mtuci.demo.service.TicketService;
import java.security.*;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

@Service
public class TicketServiceImpl implements TicketService {

    @Override
    public ApplicationTicket createTicket(String status, String info, ApplicationLicense applicationLicense,
                                          ApplicationUser applicationUser, ApplicationDevice applicationDevice) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 3);

        ApplicationTicket applicationTicket = new ApplicationTicket();

        applicationTicket.setStatus(status);
        applicationTicket.setInfo(info);
        applicationTicket.setCurrentDate(calendar.getTime());
        applicationTicket.setLifetime(calendar.getTime());

        if (applicationLicense != null) {
            calendar.setTime(applicationLicense.getFirstActivationDate());
            calendar.add(Calendar.HOUR_OF_DAY, 3);
            applicationTicket.setActivationDate(calendar.getTime());
            calendar.setTime(applicationLicense.getEndingDate());
            calendar.add(Calendar.HOUR_OF_DAY, 3);
            applicationTicket.setExpirationDate(calendar.getTime());
            applicationTicket.setLicenseBlocked(applicationLicense.isBlocked());
        }

        if (applicationUser != null) applicationTicket.setUserId(applicationUser.getId());

        if (applicationDevice != null) applicationTicket.setDeviceId(applicationDevice.getId());

        applicationTicket.setDigitalSignature(makeSignature(applicationTicket));

        return applicationTicket;

    }

    @Override
    public String makeSignature(ApplicationTicket applicationTicket) {

        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            ObjectMapper objectMapper = new ObjectMapper();
            String res = objectMapper.writeValueAsString(applicationTicket);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());

        } catch (Exception e) {

            return "Что-то пошло не так. Подпись не действительна";

        }

    }

}