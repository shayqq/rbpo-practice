package ru.mtuci.demo.request;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseActivationRequest {

    private String activationCode;
    private String name;
    private String mac_address;

}