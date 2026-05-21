package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<User> getAll(){return userRepository.findAll();}
    public User getById(Long id){return userRepository.findById(id).orElse(null);}
    public void save(User user){userRepository.save(user);}
    public void deleteByid(Long id){userRepository.deleteById(id);}
}
