package com.tallerrepair.tallerrepair;

import com.tallerrepair.tallerrepair.controller.LoginController;
import com.tallerrepair.tallerrepair.controller.MainController;
import com.tallerrepair.tallerrepair.service.AuthService;
import com.tallerrepair.tallerrepair.service.BudgetDataService;
import com.tallerrepair.tallerrepair.service.CashDataService;
import com.tallerrepair.tallerrepair.service.ProductDataService;
import com.tallerrepair.tallerrepair.service.SaleDataService;
import com.tallerrepair.tallerrepair.service.SystemSettingsService;
import com.tallerrepair.tallerrepair.session.AppSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        new AuthService().initializeDefaultSecurity();
        new BudgetDataService().ensureDemoBudgetData();
        new ProductDataService().ensureDemoInventoryData();
        new SaleDataService().ensureDemoSalesData();
        new CashDataService().ensureDemoCashSession();
        showLogin();
    }

    public void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setOnLoginSuccess(this::showMainApplication);

        Scene scene = new Scene(root, 760, 500);
        scene.getStylesheets().add(getClass().getResource("/css/auth.css").toExternalForm());

        primaryStage.setTitle(new SystemSettingsService().getCompanyProfile().name() + " - Acceso");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public void showMainApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-layout.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setOnLogout(() -> {
                AppSession.clear();
                try {
                    showLogin();
                } catch (Exception exception) {
                    throw new IllegalStateException("No se pudo volver a la pantalla de acceso.", exception);
                }
            });

            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

            primaryStage.setTitle(new SystemSettingsService().getCompanyProfile().name());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(720);
            primaryStage.show();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo cargar la pantalla principal de TallerRepair.", exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
