package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Permission;
import com.tallerrepair.tallerrepair.entity.Role;
import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import com.tallerrepair.tallerrepair.security.PasswordHasher;
import jakarta.persistence.EntityManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RolePermissionService {

    public boolean hasPermission(User user, String permissionCode) {
        return user != null && user.isActive() && user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permissionCode.equals(permission.getCode()));
    }

    public List<Role> getRolesWithPermissions() {
        try (EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            return entityManager.createQuery(
                    "SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions ORDER BY r.name", Role.class)
                    .getResultList();
        }
    }

    public List<Permission> getAllPermissions() {
        try (EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            return entityManager.createQuery("SELECT p FROM Permission p ORDER BY p.code", Permission.class)
                    .getResultList();
        }
    }

    public List<User> getActiveUsersWithRoles() {
        try (EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager()) {
            return entityManager.createQuery(
                    "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.active = true ORDER BY u.username", User.class)
                    .getResultList();
        }
    }

    public User createUser(String username, String fullName, String email, String password, Set<Long> roleIds) {
        String normalizedUsername = clean(username);
        String normalizedFullName = clean(fullName);
        String normalizedEmail = clean(email);
        if (normalizedUsername == null || normalizedFullName == null || normalizedEmail == null) {
            throw new IllegalArgumentException("Usuario, nombre completo y correo son obligatorios.");
        }
        if (!normalizedUsername.matches("[A-Za-z0-9._-]{3,80}")) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 80 caracteres: letras, números, punto, guion o guion bajo.");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        Set<Long> selectedIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        if (selectedIds.isEmpty()) {
            throw new IllegalArgumentException("Asigna al menos un rol al usuario.");
        }

        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Long usernameCount = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE LOWER(u.username) = :username", Long.class)
                    .setParameter("username", normalizedUsername.toLowerCase(Locale.ROOT))
                    .getSingleResult();
            Long emailCount = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = :email", Long.class)
                    .setParameter("email", normalizedEmail.toLowerCase(Locale.ROOT))
                    .getSingleResult();
            if (usernameCount > 0) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre de acceso.");
            }
            if (emailCount > 0) {
                throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico.");
            }

            List<Role> selectedRoles = entityManager.createQuery("SELECT r FROM Role r WHERE r.id IN :ids", Role.class)
                    .setParameter("ids", selectedIds)
                    .getResultList();
            if (selectedRoles.size() != selectedIds.size()) {
                throw new IllegalArgumentException("La selección contiene roles que ya no existen.");
            }

            User user = new User();
            user.setUsername(normalizedUsername);
            user.setFullName(normalizedFullName);
            user.setEmail(normalizedEmail);
            user.setPasswordHash(PasswordHasher.hash(password));
            user.setActive(true);
            user.setRoles(new LinkedHashSet<>(selectedRoles));
            entityManager.persist(user);
            entityManager.getTransaction().commit();
            return user;
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public User updateUser(Long userId, String username, String fullName, String email, String password, Set<Long> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("Selecciona un usuario válido.");
        }
        String normalizedUsername = clean(username);
        String normalizedFullName = clean(fullName);
        String normalizedEmail = clean(email);
        if (normalizedUsername == null || normalizedFullName == null || normalizedEmail == null) {
            throw new IllegalArgumentException("Usuario, nombre completo y correo son obligatorios.");
        }
        if (!normalizedUsername.matches("[A-Za-z0-9._-]{3,80}")) {
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 80 caracteres: letras, números, punto, guion o guion bajo.");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        if (password != null && !password.isBlank() && password.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        Set<Long> selectedIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        if (selectedIds.isEmpty()) {
            throw new IllegalArgumentException("Un usuario activo debe conservar al menos un rol.");
        }

        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            User user = entityManager.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id", User.class)
                    .setParameter("id", userId)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado ya no existe."));
            if ("admin".equals(user.getUsername()) && !"admin".equalsIgnoreCase(normalizedUsername)) {
                throw new IllegalArgumentException("No se puede cambiar el usuario de la cuenta admin.");
            }

            Long duplicateCount = entityManager.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE (LOWER(u.username) = :username OR LOWER(u.email) = :email) AND u.id <> :id", Long.class)
                    .setParameter("username", normalizedUsername.toLowerCase(Locale.ROOT))
                    .setParameter("email", normalizedEmail.toLowerCase(Locale.ROOT))
                    .setParameter("id", userId)
                    .getSingleResult();
            if (duplicateCount > 0) {
                throw new IllegalArgumentException("Ya existe otro usuario con ese nombre de acceso o correo electrónico.");
            }

            List<Role> selectedRoles = entityManager.createQuery("SELECT r FROM Role r WHERE r.id IN :ids", Role.class)
                    .setParameter("ids", selectedIds)
                    .getResultList();
            if (selectedRoles.size() != selectedIds.size()) {
                throw new IllegalArgumentException("La selección contiene roles que ya no existen.");
            }
            if ("admin".equals(user.getUsername()) && selectedRoles.stream().noneMatch(role -> "ADMINISTRADOR".equals(role.getName()))) {
                throw new IllegalArgumentException("El usuario admin debe conservar el rol ADMINISTRADOR.");
            }

            user.setUsername(normalizedUsername);
            user.setFullName(normalizedFullName);
            user.setEmail(normalizedEmail);
            if (password != null && !password.isBlank()) {
                user.setPasswordHash(PasswordHasher.hash(password));
            }
            user.setRoles(new LinkedHashSet<>(selectedRoles));
            entityManager.merge(user);
            entityManager.getTransaction().commit();
            return user;
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public void deactivateUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Selecciona un usuario válido.");
        }
        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            User user = entityManager.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id", User.class)
                    .setParameter("id", userId)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado ya no existe."));
            if ("admin".equals(user.getUsername())) {
                throw new IllegalArgumentException("No se puede desactivar la cuenta admin del sistema.");
            }
            if (user.isActive() && user.getRoles().stream().anyMatch(role -> "ADMINISTRADOR".equals(role.getName()))) {
                Long otherActiveAdministrators = entityManager.createQuery(
                                "SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE u.active = true AND r.name = 'ADMINISTRADOR' AND u.id <> :id", Long.class)
                        .setParameter("id", userId)
                        .getSingleResult();
                if (otherActiveAdministrators == 0) {
                    throw new IllegalArgumentException("No se puede desactivar al último administrador activo.");
                }
            }
            user.setActive(false);
            entityManager.merge(user);
            entityManager.getTransaction().commit();
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public Role saveRole(Long roleId, String name, String description, Set<Long> permissionIds) {
        String normalizedName = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        }
        if (!normalizedName.matches("[A-Z0-9_]+")) {
            throw new IllegalArgumentException("Usa solo letras, números y guion bajo en el nombre del rol.");
        }
        Set<Long> selectedIds = permissionIds == null ? Set.of() : Set.copyOf(permissionIds);
        if (selectedIds.isEmpty()) {
            throw new IllegalArgumentException("Asigna al menos un permiso al rol.");
        }

        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Role role = roleId == null ? new Role() : entityManager.find(Role.class, roleId);
            if (role == null) {
                throw new IllegalArgumentException("El rol seleccionado ya no existe.");
            }

                Long duplicateCount = roleId == null
                    ? entityManager.createQuery("SELECT COUNT(r) FROM Role r WHERE r.name = :name", Long.class)
                    .setParameter("name", normalizedName)
                    .getSingleResult()
                    : entityManager.createQuery("SELECT COUNT(r) FROM Role r WHERE r.name = :name AND r.id <> :roleId", Long.class)
                    .setParameter("name", normalizedName)
                    .setParameter("roleId", roleId)
                    .getSingleResult();
            if (duplicateCount > 0) {
                throw new IllegalArgumentException("Ya existe un rol con ese nombre.");
            }

            boolean administratorRole = "ADMINISTRADOR".equals(role.getName()) || "ADMINISTRADOR".equals(normalizedName);
            if (roleId != null && "ADMINISTRADOR".equals(role.getName()) && !"ADMINISTRADOR".equals(normalizedName)) {
                throw new IllegalArgumentException("No se puede cambiar el nombre del rol ADMINISTRADOR.");
            }

            List<Permission> selectedPermissions = selectedIds.isEmpty() ? List.of()
                    : entityManager.createQuery("SELECT p FROM Permission p WHERE p.id IN :ids", Permission.class)
                    .setParameter("ids", selectedIds)
                    .getResultList();
            if (selectedPermissions.size() != selectedIds.size()) {
                throw new IllegalArgumentException("La selección contiene permisos que ya no existen.");
            }
            if (administratorRole && selectedPermissions.stream().noneMatch(permission -> "SETTINGS_MANAGE".equals(permission.getCode()))) {
                throw new IllegalArgumentException("ADMINISTRADOR debe conservar SETTINGS_MANAGE.");
            }

            role.setName(normalizedName);
            role.setDescription(description == null || description.isBlank() ? null : description.trim());
            role.setPermissions(new LinkedHashSet<>(selectedPermissions));
            if (roleId == null) {
                entityManager.persist(role);
            }
            entityManager.getTransaction().commit();
            return role;
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public User saveUserRoles(Long userId, Set<Long> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("Selecciona un usuario.");
        }
        Set<Long> selectedIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);

        EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            entityManager.getTransaction().begin();
            User user = entityManager.createQuery(
                            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id", User.class)
                    .setParameter("id", userId)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado ya no existe."));
            if (user.isActive() && selectedIds.isEmpty()) {
                throw new IllegalArgumentException("Un usuario activo debe conservar al menos un rol.");
            }

            List<Role> selectedRoles = selectedIds.isEmpty() ? List.of()
                    : entityManager.createQuery("SELECT r FROM Role r WHERE r.id IN :ids", Role.class)
                    .setParameter("ids", selectedIds)
                    .getResultList();
            if (selectedRoles.size() != selectedIds.size()) {
                throw new IllegalArgumentException("La selección contiene roles que ya no existen.");
            }
            if ("admin".equals(user.getUsername()) && selectedRoles.stream().noneMatch(role -> "ADMINISTRADOR".equals(role.getName()))) {
                throw new IllegalArgumentException("El usuario admin debe conservar el rol ADMINISTRADOR.");
            }

            user.setRoles(new LinkedHashSet<>(selectedRoles));
            entityManager.merge(user);
            entityManager.getTransaction().commit();
            return user;
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}