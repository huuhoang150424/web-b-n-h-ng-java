package com.nhhoang.e_commerce.repository;

import com.nhhoang.e_commerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,String> {


}
