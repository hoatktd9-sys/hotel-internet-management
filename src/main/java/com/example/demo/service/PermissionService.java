package com.example.demo.service;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    public PermissionService(PermissionRepository repository) {
        this.permissionRepository = repository;
    }
    public PermissionRepository getRepository() {
        return permissionRepository;
    }
    public Permission save(Permission permission){return permissionRepository.save(permission);}
    public void delete(Permission permission){permissionRepository.delete(permission);}
    public Permission findById(Long id){return permissionRepository.findById(id).orElse(null);}
    public List<Permission> findAll(){return permissionRepository.findAll();}
    public Optional<Permission>  findByName(String name){return permissionRepository.findByPermissionName(name);}

}
