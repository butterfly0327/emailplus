package com.backend.backend.domain.mapping.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Long id;
    private String email;
    private String username;
    private String password;
    private Boolean deleteCheck;
}

