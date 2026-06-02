package com.example.demo.specification;

import com.example.demo.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("username")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null || email.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.trim().isEmpty()) return null;
            return cb.equal(cb.lower(root.join("roles").get("roleName")), role.toLowerCase());
        };
    }
}
