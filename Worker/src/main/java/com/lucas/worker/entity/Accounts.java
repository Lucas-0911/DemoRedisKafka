package com.lucas.worker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
public class Accounts {
    @Id
    private Long id;
    private String username;
    private String password;
    private Status status;
    private Date created;
    private Date updated;

    public enum Status {
        CREATE, ACTIVE, LOCKED;
    }
}
