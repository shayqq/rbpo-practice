package ru.mtuci.demo.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {

    Long id;

}