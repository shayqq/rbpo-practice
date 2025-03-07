package ru.mtuci.demo.service;

import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;
import java.util.List;
import java.util.Optional;

public interface LicenseService {

    Optional<ApplicationLicense> getLicenseById(Long id);
    Long createLicense(Long productId, Long ownerId, Long licenseTypeId, ApplicationUser user, Long count);
    ApplicationTicket getActiveLicensesForDevice(ApplicationDevice device, String code);
    List<String> getAllLicenseForDevice(ApplicationDevice device);
    List<String> getAllLicensesRenewalForUser(ApplicationUser user);
    ApplicationTicket createTicket(ApplicationUser user, ApplicationDevice device,
                                   ApplicationLicense license, String info, String status);
    ApplicationTicket activateLicense(String code, ApplicationDevice device, ApplicationUser user);
    String updateLicense(Long id, Long ownerId, Long productId, Long typeId, Boolean isBlocked,
                         String description, Long deviceCount);
    ApplicationTicket renewalLicense(String code, ApplicationUser user);

}