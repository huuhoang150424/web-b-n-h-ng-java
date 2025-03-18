package com.nhhoang.e_commerce.service;


import com.nhhoang.e_commerce.dto.requests.UserRequests;
import com.nhhoang.e_commerce.entity.User;
import com.nhhoang.e_commerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


//    public User createUser(UserRequests request) {
//        User newUser=new User();
//        newUser.setName(request.getName());
//        newUser.setEmail(request.getEmail());
//        newUser.setAddress(request.getAddress());
//        newUser.setName(request.getName());
//        newUser.setName(request.getName());
//
//    }
}
