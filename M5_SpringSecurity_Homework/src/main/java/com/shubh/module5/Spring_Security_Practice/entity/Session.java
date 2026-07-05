package com.shubh.module5.Spring_Security_Practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_sessions")
public class Session {
    @Id
    Long userId;

    String token;

    LocalDateTime createdAt;

    LocalDateTime expiredAt;
}
