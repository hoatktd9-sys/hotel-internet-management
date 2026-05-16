package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired private RoleRepository roleRepository;

    public RoleRepository getRoleRepository() {
        return roleRepository;
    }
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public void saveRole(Role role) {
        roleRepository.save(role);
    }
    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
