package com.tallerrepair.tallerrepair.controller;

import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.service.AuthService;
import com.tallerrepair.tallerrepair.service.SystemSettingsService;
import com.tallerrepair.tallerrepair.session.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Label loginBrandLabel;

    private final AuthService authService = new AuthService();
    private final SystemSettingsService systemSettingsService = new SystemSettingsService();
    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void initialize() {
        loginButton.setDefaultButton(true);
        loginBrandLabel.setText(systemSettingsService.getCompanyProfile().name());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        Optional<User> user = authService.login(username, password);
        if (user.isEmpty()) {
            errorLabel.setText("Usuario o contraseña incorrectos.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        AppSession.setCurrentUser(user.get());
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }
}
