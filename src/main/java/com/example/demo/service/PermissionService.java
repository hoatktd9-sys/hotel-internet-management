package com.example.demo.service;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private final PermissionRepository repository;
    public PermissionService(PermissionRepository repository) {
        this.repository = repository;
    }
    public PermissionRepository getRepository() {
        return repository;
    }
    public void save(Permission permission){repository.save(permission);}
    public void delete(Permission permission){repository.delete(permission);}
    public Permission findById(Long id){return repository.findById(id).orElse(null);}
    public Iterable<Permission> findAll(){return repository.findAll();}

}
