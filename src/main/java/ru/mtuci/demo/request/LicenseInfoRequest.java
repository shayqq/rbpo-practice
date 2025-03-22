package ru.mtuci.demo.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseInfoRequest {

    private String name;
    private String mac_address;
    private String code;

}