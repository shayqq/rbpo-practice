package ru.mtuci.demo.service;

import ru.mtuci.demo.model.ApplicationDevice;
import ru.mtuci.demo.model.ApplicationLicense;
import ru.mtuci.demo.model.ApplicationTicket;
import ru.mtuci.demo.model.ApplicationUser;

public interface TicketService {

    ApplicationTicket createTicket(String status, String info, ApplicationLicense applicationLicense,
                                          ApplicationUser applicationUser, ApplicationDevice applicationDevice);
    String makeSignature(ApplicationTicket applicationTicket);

}