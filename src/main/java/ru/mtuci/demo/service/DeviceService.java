package ru.mtuci.demo.service;

import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationUser;
import java.util.Optional;

public interface DeviceService {

    Optional<ApplicationDevice> getDeviceByInfo(ApplicationUser user, String mac_address, String name);
    void deleteLastDevice(ApplicationUser user);
    ApplicationDevice registerOrUpdateDevice(String mac, String name, ApplicationUser user);

}