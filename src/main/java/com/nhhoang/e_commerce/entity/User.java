package com.nhhoang.e_commerce.entity;

import com.nhhoang.e_commerce.utils.JsonListConverter;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @Column(length = 36, unique = true, nullable = false)
    private String id = UUID.randomUUID().toString();

    @Column(length = 20)
    private String name;

    @Column(length = 50, unique = true)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 10, nullable = true)
    private String phone;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(length = 150)
    private String avatar;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> address;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (birthDate == null) {
            birthDate = LocalDate.now();
        }
        if (avatar == null) {
            avatar = "https://res.cloudinary.com/dw3oj3iju/image/upload/v1709749732/chat_app/b1rj7epnhdqo6t7mcu5w.jpg";  // Default avatar
        }
        if (gender == null) {
            gender = Gender.OTHER;
        }
        if (phone == null) {
            phone = "123457890";
        }
        if (isAdmin == null) {
            isAdmin = false;
        }
        if (address == null) {
            address = List.of();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Gender {
        MALE("Nam"),
        FEMALE("Nữ"),
        OTHER("Khác");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}