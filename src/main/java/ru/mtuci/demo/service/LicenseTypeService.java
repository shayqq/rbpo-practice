package ru.mtuci.demo.service;

import ru.mtuci.demo.model.ApplicationLicenseType;
import java.util.Optional;

public interface LicenseTypeService {

    Optional<ApplicationLicenseType> getLicenseTypeById(Long id);
    Long createLicenseType(Long duration, String description, String name);
    String upadteLicenseType(Long id, Long duration, String description, String name);

}