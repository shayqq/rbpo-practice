package ru.mtuci.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationTicket {

    private String status;
    private String info;
    private Date currentDate;
    private Date lifetime;
    private Date activationDate;
    private Date expirationDate;
    private Long userId;
    private Long deviceId;
    private boolean licenseBlocked;
    private String digitalSignature;

}