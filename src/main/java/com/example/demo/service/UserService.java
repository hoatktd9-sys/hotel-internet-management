package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<User> findAll(){return userRepository.findAll();}
    public User getById(Long id){return userRepository.findById(id).orElse(null);}
    public void save(User user){userRepository.save(user);}
    public void deleteById(Long id){userRepository.deleteById(id);}
    public Optional<User> findByUsername(String username){return userRepository.findByUsername(username); }
    public Optional<User> findByEmail(String email){return userRepository.findByEmail(email);}
    public Page<User> searchUser (String name, String email, String Role, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Specification<User>  spec =  Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasEmail(email))
                .and(UserSpecification.hasRole(Role));
        return userRepository.findAll(spec, pageable);
    }
     public User findById(Long id){return userRepository.findById(id).orElse(null);}
}
