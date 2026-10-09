package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Permission;
import com.tallerrepair.tallerrepair.entity.Role;
import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import com.tallerrepair.tallerrepair.repository.UserRepository;
import com.tallerrepair.tallerrepair.security.PasswordHasher;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public Optional<User> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> optionalUser = userRepository.findByUsername(username.trim());
        if (optionalUser.isEmpty()) {
            return Optional.empty();
        }

        User user = optionalUser.get();
        if (!user.isActive()) {
            return Optional.empty();
        }

        if (!PasswordHasher.matches(password, user.getPasswordHash())) {
            return Optional.empty();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return Optional.of(user);
    }

    public void initializeDefaultSecurity() {
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();

            List<String> permissionCodes = List.of(
                    "CLIENTES_VIEW",
                    "CLIENTES_CREATE",
                    "CLIENTES_UPDATE",
                    "CLIENTES_DELETE",
                    "ORDERS_VIEW",
                    "ORDERS_CREATE",
                    "ORDERS_UPDATE",
                    "ORDERS_DELETE",
                    "INVENTORY_VIEW",
                    "INVENTORY_CREATE",
                    "INVENTORY_UPDATE",
                    "SALES_VIEW",
                    "SALES_CREATE",
                    "CASH_VIEW",
                    "CASH_OPEN",
                    "CASH_CLOSE",
                    "REPORTS_VIEW",
                    "USERS_CREATE",
                    "USERS_UPDATE",
                    "USERS_DELETE",
                    "SETTINGS_MANAGE"
            );

            Set<Permission> allPermissions = new LinkedHashSet<>();
            for (String code : permissionCodes) {
                Permission permission = entityManager
                        .createQuery("SELECT p FROM Permission p WHERE p.code = :code", Permission.class)
                        .setParameter("code", code)
                        .getResultStream()
                        .findFirst()
                        .orElseGet(() -> {
                            Permission newPermission = new Permission();
                            newPermission.setCode(code);
                            newPermission.setName(code.replace("_", " ").toLowerCase());
                            newPermission.setDescription("Permiso del módulo " + code);
                            entityManager.persist(newPermission);
                            return newPermission;
                        });
                allPermissions.add(permission);
            }

            createRoleIfMissing(entityManager, "ADMINISTRADOR", "Administrador del sistema", allPermissions);
            createRoleIfMissing(entityManager, "GERENTE", "Gerencia general del taller", allPermissions);
            createRoleIfMissing(entityManager, "TECNICO", "Técnico de reparación", permissionCodesSubset(allPermissions, List.of(
                    "CLIENTES_VIEW", "ORDERS_VIEW", "ORDERS_CREATE", "ORDERS_UPDATE", "INVENTORY_VIEW", "REPORTS_VIEW"
            )));
            createRoleIfMissing(entityManager, "RECEPCION", "Recepción de clientes y órdenes", permissionCodesSubset(allPermissions, List.of(
                    "CLIENTES_VIEW", "CLIENTES_CREATE", "CLIENTES_UPDATE", "ORDERS_VIEW", "ORDERS_CREATE", "ORDERS_UPDATE"
            )));
            createRoleIfMissing(entityManager, "CAJERO", "Atención y cobranza", permissionCodesSubset(allPermissions, List.of(
                    "SALES_VIEW", "SALES_CREATE", "CASH_VIEW", "CASH_OPEN", "CASH_CLOSE", "REPORTS_VIEW"
            )));

                Set<String> userAdministrationRoles = Set.of("ADMINISTRADOR", "GERENTE");
                for (String roleName : userAdministrationRoles) {
                Role role = entityManager.createQuery("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
                role.getPermissions().addAll(permissionCodesSubset(allPermissions,
                    List.of("USERS_CREATE", "USERS_UPDATE", "USERS_DELETE")));
                }

            if (entityManager
                    .createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", "admin")
                    .getSingleResult() == 0L) {
                Role adminRole = entityManager
                        .createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
                        .setParameter("name", "ADMINISTRADOR")
                        .getSingleResult();

                User admin = new User();
                admin.setUsername("admin");
                admin.setFullName("Administrador del sistema");
                admin.setEmail("admin@tallerrepair.local");
                admin.setPasswordHash(PasswordHasher.hash("admin123"));
                admin.setActive(true);
                admin.setRoles(Set.of(adminRole));
                entityManager.persist(admin);
            }

            entityManager.getTransaction().commit();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    private void createRoleIfMissing(EntityManager entityManager, String roleName, String description, Set<Permission> permissions) {
        boolean exists = entityManager
                .createQuery("SELECT COUNT(r) FROM Role r WHERE r.name = :name", Long.class)
                .setParameter("name", roleName)
                .getSingleResult() > 0L;

        if (exists) {
            return;
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDescription(description);
        role.setPermissions(permissions);
        entityManager.persist(role);
    }

    private Set<Permission> permissionCodesSubset(Set<Permission> available, List<String> codes) {
        Set<Permission> subset = new LinkedHashSet<>();
        for (Permission permission : available) {
            if (codes.contains(permission.getCode())) {
                subset.add(permission);
            }
        }
        return subset;
    }
}
