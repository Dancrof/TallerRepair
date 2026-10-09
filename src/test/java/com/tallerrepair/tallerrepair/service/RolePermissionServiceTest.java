package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Role;
import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.persistence.JpaUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionServiceTest {

    @Test
    void shouldLoadRolesPermissionsAndUsersAndEnforceSecurityGuards() {
        new AuthService().initializeDefaultSecurity();
        RolePermissionService service = new RolePermissionService();

        var permissions = service.getAllPermissions();
        var roles = service.getRolesWithPermissions();
        var users = service.getActiveUsersWithRoles();
        assertFalse(permissions.isEmpty());
        assertFalse(roles.isEmpty());
        assertFalse(users.isEmpty());

        assertTrue(permissions.stream().anyMatch(permission -> "USERS_CREATE".equals(permission.getCode())));
        assertTrue(permissions.stream().anyMatch(permission -> "USERS_UPDATE".equals(permission.getCode())));
        assertTrue(permissions.stream().anyMatch(permission -> "USERS_DELETE".equals(permission.getCode())));

        Role adminRole = roles.stream().filter(role -> "ADMINISTRADOR".equals(role.getName())).findFirst().orElseThrow();
        assertTrue(adminRole.getPermissions().stream().anyMatch(permission -> "USERS_CREATE".equals(permission.getCode())));
        assertTrue(adminRole.getPermissions().stream().anyMatch(permission -> "USERS_UPDATE".equals(permission.getCode())));
        assertTrue(adminRole.getPermissions().stream().anyMatch(permission -> "USERS_DELETE".equals(permission.getCode())));
        Role managerRole = roles.stream().filter(role -> "GERENTE".equals(role.getName())).findFirst().orElseThrow();
        assertTrue(managerRole.getPermissions().stream().anyMatch(permission -> "USERS_CREATE".equals(permission.getCode())));
        assertTrue(managerRole.getPermissions().stream().anyMatch(permission -> "USERS_UPDATE".equals(permission.getCode())));
        assertTrue(managerRole.getPermissions().stream().anyMatch(permission -> "USERS_DELETE".equals(permission.getCode())));
        assertTrue(service.hasPermission(users.get(0), "SETTINGS_MANAGE"));
        assertThrows(IllegalArgumentException.class, () -> service.saveRole(
                adminRole.getId(), adminRole.getName(), adminRole.getDescription(),
                adminRole.getPermissions().stream().filter(permission -> !"SETTINGS_MANAGE".equals(permission.getCode()))
                        .map(permission -> permission.getId()).collect(Collectors.toSet())));

        Role editableRole = roles.stream().filter(role -> "GERENTE".equals(role.getName())).findFirst().orElseThrow();
        var editablePermissionIds = editableRole.getPermissions().stream().map(permission -> permission.getId())
            .collect(Collectors.toSet());
        assertTrue(service.saveRole(editableRole.getId(), editableRole.getName(), editableRole.getDescription(),
            editablePermissionIds).getId().equals(editableRole.getId()));

        User admin = users.stream().filter(user -> "admin".equals(user.getUsername())).findFirst().orElseThrow();
        var adminRoleIds = admin.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        assertTrue(service.saveUserRoles(admin.getId(), adminRoleIds).getId().equals(admin.getId()));
        assertThrows(IllegalArgumentException.class, () -> service.saveUserRoles(admin.getId(),
                admin.getRoles().stream().filter(role -> !"ADMINISTRADOR".equals(role.getName()))
                        .map(Role::getId).collect(Collectors.toSet())));
        assertTrue(admin.getRoles().stream().anyMatch(role -> "ADMINISTRADOR".equals(role.getName())));

        String testRoleName = "TEST_ROLE_PERMISSION_FLOW";
        try {
            Role createdRole = service.saveRole(null, testRoleName, "Prueba temporal",
                    Set.of(permissions.get(0).getId()));
            assertTrue(service.getRolesWithPermissions().stream()
                    .anyMatch(role -> role.getId().equals(createdRole.getId())));
        } finally {
            EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
            try {
                entityManager.getTransaction().begin();
                entityManager.createQuery("DELETE FROM Role r WHERE r.name = :name")
                        .setParameter("name", testRoleName)
                        .executeUpdate();
                entityManager.getTransaction().commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    @Test
    void shouldCreateUserWithRoleAndAllowLogin() {
        new AuthService().initializeDefaultSecurity();
        RolePermissionService service = new RolePermissionService();
        Role role = service.getRolesWithPermissions().stream()
                .filter(item -> "RECEPCION".equals(item.getName())).findFirst().orElseThrow();
        String username = "test.user." + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.test";
        String password = "TestPassword123";

        try {
            User created = service.createUser(username, "Usuario de prueba", email, password, Set.of(role.getId()));
            assertEquals(username, created.getUsername());
            assertEquals("RECEPCION", created.getRoles().iterator().next().getName());
            assertTrue(new AuthService().login(username, password).isPresent());
                String newPassword = "UpdatedPassword456";
                User updated = service.updateUser(created.getId(), username, "Nombre actualizado", email,
                    newPassword, Set.of(role.getId()));
                assertEquals("Nombre actualizado", updated.getFullName());
                assertTrue(new AuthService().login(username, newPassword).isPresent());
            assertThrows(IllegalArgumentException.class,
                    () -> service.createUser(username, "Duplicado", "other@example.test", password, Set.of(role.getId())));
            assertThrows(IllegalArgumentException.class,
                    () -> service.createUser("valid.user", "Usuario", "valid@example.test", "short", Set.of(role.getId())));
                service.deactivateUser(created.getId());
                assertTrue(new AuthService().login(username, newPassword).isEmpty());
        } finally {
            EntityManager entityManager = JpaUtil.getEntityManagerFactory().createEntityManager();
            try {
                entityManager.getTransaction().begin();
                entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username = :username)")
                        .setParameter("username", username).executeUpdate();
                entityManager.createQuery("DELETE FROM User u WHERE u.username = :username")
                        .setParameter("username", username).executeUpdate();
                entityManager.getTransaction().commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }
}