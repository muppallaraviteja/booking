package com.ravi.booking.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ravi.booking.model.UserEntity;

@Component
public class UserRepository {
  //email
  Map<String, UserEntity> emailMap = new HashMap<>();
  Map<String, UserEntity> idMap = new HashMap<>();
  public UserEntity findByEmail(String email){
    if(emailMap.containsKey(email))
      return emailMap.get(email);
    return null;

  }

  public UserEntity save(UserEntity user){
    idMap.put(user.getId(), user);
    emailMap.put(user.getEmail(), user);
    return user;

  }
}