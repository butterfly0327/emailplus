package com.backend.backend.domain.auth.entity;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Account {
    private Long id;
    private String email;
    private String username;
    private String password;
    private Boolean deleteCheck;

}
