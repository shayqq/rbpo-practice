package ru.mtuci.demo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.*;
import ru.mtuci.demo.repository.DeviceLicenseRepository;
import ru.mtuci.demo.repository.LicenseRepository;
import ru.mtuci.demo.service.LicenseService;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseTypeServiceImpl licenseTypeService;
    private final ProductServiceImpl productService;
    private final DeviceLicenseServiceImpl deviceLicenseService;
    private final LicenseHistoryServiceImpl licenseHistoryService;
    private final UserDetailsServiceImpl userDetailsService;
    private final DeviceServiceImpl deviceService;
    private final DeviceLicenseRepository deviceLicenseRepository;

    @Override
    public Optional<ApplicationLicense> getLicenseById(Long id) {
        return licenseRepository.findById(id);
    }

    @Override
    public Long createLicense(Long productId, Long ownerId, Long licenseTypeId, ApplicationUser user, Long count) {
        ApplicationLicenseType licenseType = licenseTypeService.getLicenseTypeById(licenseTypeId).get();
        ApplicationProduct product = productService.getProductById(productId).get();
        ApplicationUser ownerUser = userDetailsService.getUserById(ownerId).get();
        ApplicationLicense newLicense = new ApplicationLicense();
        String code = String.valueOf(UUID.randomUUID());
        while (licenseRepository.findByCode(code).isPresent()) code = String.valueOf(UUID.randomUUID());
        newLicense.setCode(code);
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(product.isBlocked());
        newLicense.setDeviceCount(count);
        newLicense.setOwner(ownerUser);
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(licenseType.getDescription());

        licenseRepository.save(newLicense);

        licenseHistoryService.createNewRecord("Не активирована", "Создана новая лицензия", user,
                licenseRepository.findTopByOrderByIdDesc().get());

        return licenseRepository.findTopByOrderByIdDesc().get().getId();
    }

    @Override
    public ApplicationTicket getActiveLicensesForDevice(ApplicationDevice device, String code) {
        List<ApplicationDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicenseById(device);
        List<Long> licenseIds = applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getId() : null)
                .collect(Collectors.toList());
        Optional<ApplicationLicense> applicationLicense = licenseRepository.findByIdInAndCode(licenseIds, code);

        ApplicationTicket ticket = new ApplicationTicket();

        if (applicationLicense.isEmpty()) {
            ticket.setInfo("License was not found");
            ticket.setStatus("Error");
            return ticket;
        }
        ticket = createTicket(applicationLicense.get().getUser(), device, applicationLicense.get(),
                "Info about license", "OK");

        return ticket;
    }

    @Override
    public List<String> getAllLicenseForDevice(ApplicationDevice device) {
        List<ApplicationDeviceLicense> applicationDeviceLicensesList = deviceLicenseService.getAllLicenseById(device);
        return applicationDeviceLicensesList.stream()
                .map(license -> license.getLicense() != null ? license.getLicense().getCode() : null)
                .toList();
    }

    @Override
    public List<String> getAllLicensesRenewalForUser(ApplicationUser user) {
        List<ApplicationLicense> applicationLicenseList = licenseRepository.findByOwnerId(user);
        return applicationLicenseList.stream()
                .map(license -> license != null ? license.getCode() : null)
                .toList();
    }

    private String makeSignature(ApplicationTicket ticket) {
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            ObjectMapper objectMapper = new ObjectMapper();
            String res = objectMapper.writeValueAsString(ticket);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(res.getBytes());

            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch (Exception e){
            return "Что-то пошло не так. Подпись не действительна";
        }
    }

    @Override
    public ApplicationTicket createTicket(ApplicationUser user, ApplicationDevice device,
                                          ApplicationLicense license, String info, String status) {
        ApplicationTicket ticket = new ApplicationTicket();
        ticket.setCurrentDate(new Date());

        if (user != null) ticket.setUserId(user.getId());

        if (device != null) ticket.setDeviceId(device.getId());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1);
        ticket.setLifetime(calendar.getTime());

        if (license != null) {
            ticket.setActivationDate(license.getFirstActivationDate());
            ticket.setExpirationDate(license.getEndingDate());
            ticket.setLicenseBlocked(license.isBlocked());
        }

        ticket.setInfo(info);
        ticket.setStatus(status);
        ticket.setDigitalSignature(makeSignature(ticket));
        return ticket;
    }

    @Override
    public ApplicationTicket activateLicense(String code, ApplicationDevice device, ApplicationUser user) {
        ApplicationTicket ticket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (license.isEmpty()) {
            ticket.setInfo("Неправильный код активации");
            ticket.setStatus("Ошибка");
            if (deviceLicenseRepository.findByDeviceId(device.getId()).isEmpty()) deviceService.deleteLastDevice(user);
            return ticket;
        }

        ApplicationLicense newLicense = license.get();
        if (!deviceLicenseRepository.findByDeviceIdAndLicenseId(device.getId(), newLicense.getId()).isEmpty()) {
            ticket.setInfo("Активация невозможна");
            ticket.setStatus("Ошибка");
            return ticket;
        }

        if (newLicense.isBlocked()
                || (newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate()))
                || (newLicense.getUser() != null && !Objects.equals(newLicense.getUser().getId(), user.getId()))
                || deviceLicenseService.getDeviceCountForLicense(newLicense.getId()) >= newLicense.getDeviceCount()) {
            ticket.setInfo("Активация невозможна");
            ticket.setStatus("Ошибка");
            if (deviceLicenseRepository.findByDeviceId(device.getId()).isEmpty()) deviceService.deleteLastDevice(user);
            return ticket;
        }

        if (newLicense.getFirstActivationDate() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            newLicense.setFirstActivationDate(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
            newLicense.setEndingDate(calendar.getTime());
            newLicense.setUser(user);
        }

        deviceLicenseService.createDeviceLicense(newLicense, device);
        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Активирована", "Действительная лицензия", user, newLicense);

        ticket = createTicket(user, device, newLicense, "Лицензия была успешно активирована", "OK");

        return ticket;
    }

    @Override
    public String updateLicense(Long id, Long ownerId, Long productId, Long typeId, Boolean isBlocked,
                                String description, Long deviceCount) {
        Optional<ApplicationLicense> license = getLicenseById(id);
        if (license.isEmpty()) return "License Not Found";

        ApplicationLicense newLicense = license.get();
        newLicense.setCode(String.valueOf(UUID.randomUUID()));

        if (productService.getProductById(productId).isEmpty()) return "Product Not Found";

        newLicense.setProduct(productService.getProductById(productId).get());

        if (licenseTypeService.getLicenseTypeById(typeId).isEmpty()) return "License Type Not Found";

        newLicense.setLicenseType(licenseTypeService.getLicenseTypeById(typeId).get());
        newLicense.setDuration(licenseTypeService.getLicenseTypeById(typeId).get().getDefaultDuration());
        newLicense.setBlocked(isBlocked);
        newLicense.setOwner(userDetailsService.getUserById(ownerId).get());
        newLicense.setDescription(description);
        newLicense.setDeviceCount(deviceCount);

        licenseRepository.save(newLicense);

        return "OK";
    }

    @Override
    public ApplicationTicket renewalLicense(String code, ApplicationUser user) {
        ApplicationTicket ticket = new ApplicationTicket();
        Optional<ApplicationLicense> license = licenseRepository.findByCode(code);
        if (license.isEmpty()) {
            ticket.setInfo("The license key is not valid");
            ticket.setStatus("Error");
            return ticket;
        }
        ApplicationLicense newLicense = license.get();
        if (newLicense.isBlocked()
                || newLicense.getEndingDate() != null && new Date().after(newLicense.getEndingDate())
                || !Objects.equals(newLicense.getOwner().getId(), user.getId())
                || newLicense.getFirstActivationDate() == null) {
            ticket.setInfo("It is not possible to renew the license");
            ticket.setStatus("Error");
            return ticket;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newLicense.getEndingDate());
        calendar.add(Calendar.DAY_OF_MONTH, Math.toIntExact(newLicense.getDuration()));
        newLicense.setEndingDate(calendar.getTime());

        licenseRepository.save(newLicense);
        licenseHistoryService.createNewRecord("Renewal", "Valid license", user,
                newLicense);

        ticket = createTicket(user, null, newLicense, "The license has been successfully renewed", "OK");

        return ticket;
    }

}