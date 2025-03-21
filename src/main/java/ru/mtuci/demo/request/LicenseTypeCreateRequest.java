package ru.mtuci.demo.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseTypeCreateRequest {

    private String name;
    private Long defaultDuration;
    private String description;

}