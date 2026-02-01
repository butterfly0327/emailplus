package com.backend.backend.domain.mapping.entity;

import com.backend.backend.domain.auth.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mapping {
    private Long id;
    private Account account;
    private String name;
    private Boolean isActive;
    private ChangeType changeType;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ChangeType {
        CREATE, UPDATE, APPLY_PRESET, RESET, ROLLBACK
    }
}
