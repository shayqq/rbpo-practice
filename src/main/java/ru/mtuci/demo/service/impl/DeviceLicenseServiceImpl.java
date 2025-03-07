package ru.mtuci.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationDeviceLicense;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.repository.DeviceLicenseRepository;
import ru.mtuci.demo.service.DeviceLicenseService;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceLicenseServiceImpl implements DeviceLicenseService {

    private final DeviceLicenseRepository deviceLicenseRepository;

    @Override
    public Long getDeviceCountForLicense(Long licenseId) {
        return deviceLicenseRepository.countByLicenseId(licenseId);
    }

    @Override
    public List<ApplicationDeviceLicense> getAllLicenseById(ApplicationDevice device) {
        return deviceLicenseRepository.findByDeviceId(device.getId());
    }

    @Override
    public ApplicationDeviceLicense createDeviceLicense(ApplicationLicense license, ApplicationDevice device) {
        ApplicationDeviceLicense newLicense = new ApplicationDeviceLicense();
        newLicense.setLicense(license);
        newLicense.setDevice(device);
        newLicense.setActivationDate(new Date());
        return deviceLicenseRepository.save(newLicense);
    }

}