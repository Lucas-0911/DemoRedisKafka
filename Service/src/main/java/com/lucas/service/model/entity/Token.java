package com.lucas.service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "`token`")
@RequiredArgsConstructor
public class Token implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NonNull
    private Accounts account;

    @Column(name = "`key`", length = 100, nullable = false, unique = true)
    @NonNull
    private String key;

    @Column(name = "`type`", nullable = false)
    @Enumerated(EnumType.STRING)
    @NonNull
    private Type type;

    @Column(name = "expired_date_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NonNull
    private Date expiredDateTime;

    public enum Type {
        REFRESH_TOKEN, REGISTER, FORGOT_PASSWORD;
    }
}