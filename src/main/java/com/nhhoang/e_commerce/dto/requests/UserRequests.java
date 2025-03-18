package com.nhhoang.e_commerce.dto.requests;

import jakarta.persistence.Column;

public class UserRequests {
    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    private Boolean gender;

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public Boolean getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
