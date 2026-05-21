package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public RoleRepository getRoleRepository() {
        return roleRepository;
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
    public void deleteRoleById(Long id) {
        roleRepository.deleteById(id);
    }
    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
}
