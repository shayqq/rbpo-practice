package ru.mtuci.demo.request;

import lombok.*;
import ru.mtuci.demo.model.ApplicationRole;
import java.util.UUID;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUser {

    private UUID id;
    private String username;
    private String email;
    private ApplicationRole role;

}