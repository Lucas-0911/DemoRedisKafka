package com.lucas.service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@SuperBuilder
@Table(name = "accounts")
public class Accounts {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date updated;

    enum Status {
        CREATE, ACTIVE, LOCKED;
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = Status.CREATE;
        }
    }

}
