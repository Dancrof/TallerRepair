package com.tallerrepair.tallerrepair.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    @FXML
    private Label pageTitle;

    @FXML
    private Label pageSubtitle;

    @FXML
    private Label statusBadge;

    @FXML
    private StackPane contentPane;

    private final Map<String, Node> moduleViews = new LinkedHashMap<>();
    private Button activeButton;

    @FXML
    private void initialize() {
        moduleViews.put("dashboard", createModuleView(
                "Dashboard",
                "Resumen general del taller",
                "Indicadores clave del negocio y control operativo del día."
        ));
        moduleViews.put("clientes", createModuleView(
                "Clientes",
                "Gestión de clientes",
                "Listado, búsqueda, filtros y seguimiento de historial técnico."
        ));
        moduleViews.put("equipos", createModuleView(
                "Equipos",
                "Inventario técnico",
                "Registro de equipos, marcas, seriales y mantenimiento asociado."
        ));
        moduleViews.put("ordenes", createModuleView(
                "Órdenes",
                "Control de reparaciones",
                "Seguimiento del flujo completo desde recepción hasta entrega."
        ));
        moduleViews.put("inventario", createModuleView(
                "Inventario",
                "Productos y proveedores",
                "Control de stock, movimientos y productos con stock bajo."
        ));
        moduleViews.put("ventas", createModuleView(
                "Ventas",
                "Punto de venta",
                "Cobros, productos y comprobantes de venta."
        ));
        moduleViews.put("caja", createModuleView(
                "Caja",
                "Control financiero",
                "Apertura, cierre, ingresos, egresos y arqueo de caja."
        ));
        moduleViews.put("reportes", createModuleView(
                "Reportes",
                "Estadísticas y exportación",
                "Informes, indicadores y análisis de rendimiento del taller."
        ));
        moduleViews.put("configuracion", createModuleView(
                "Configuración",
                "Ajustes del sistema",
                "Empresa, usuarios, permisos, base de datos y respaldos."
        ));

        renderModule("dashboard");
    }

    @FXML
    private void onMenuClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String moduleKey = String.valueOf(clickedButton.getUserData());

        if (moduleKey == null || moduleKey.isBlank()) {
            return;
        }

        updateActiveButton(clickedButton);
        renderModule(moduleKey);
    }

    private void updateActiveButton(Button selectedButton) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }

        activeButton = selectedButton;
        activeButton.getStyleClass().add("active");
    }

    private void renderModule(String moduleKey) {
        Node view = moduleViews.getOrDefault(moduleKey, moduleViews.get("dashboard"));
        contentPane.getChildren().setAll(view);

        switch (moduleKey) {
            case "dashboard" -> setHeader("Dashboard", "Resumen general del taller");
            case "clientes" -> setHeader("Clientes", "Gestión y seguimiento de clientes");
            case "equipos" -> setHeader("Equipos", "Inventario técnico por cliente");
            case "ordenes" -> setHeader("Órdenes", "Control del flujo de servicio");
            case "inventario" -> setHeader("Inventario", "Productos, costos y proveedores");
            case "ventas" -> setHeader("Ventas", "Punto de venta y comprobantes");
            case "caja" -> setHeader("Caja", "Apertura, cierre y arqueo");
            case "reportes" -> setHeader("Reportes", "Indicadores del taller");
            case "configuracion" -> setHeader("Configuración", "Parámetros del sistema");
            default -> setHeader("Dashboard", "Resumen general del taller");
        }
    }

    private void setHeader(String title, String subtitle) {
        pageTitle.setText(title);
        pageSubtitle.setText(subtitle);
        statusBadge.setText("Sistema local");
    }

    private Node createModuleView(String title, String summary, String description) {
        Label header = new Label(title);
        header.getStyleClass().add("module-title");

        Label summaryLabel = new Label(summary);
        summaryLabel.getStyleClass().add("module-summary");

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("module-description");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28));
        content.setStyle("-fx-background-color: transparent;");

        HBox actions = new HBox();
        actions.setSpacing(12);

        Button primaryAction = new Button("Nuevo");
        primaryAction.getStyleClass().add("primary-button");

        Button secondaryAction = new Button("Ver detalle");
        secondaryAction.getStyleClass().add("secondary-button");

        actions.getChildren().addAll(primaryAction, secondaryAction);

        VBox summaryBox = new VBox(12);
        summaryBox.getStyleClass().add("info-panel");
        summaryBox.setPadding(new Insets(20));
        summaryBox.getChildren().addAll(header, summaryLabel, descriptionLabel, actions);

        HBox cards = new HBox(18);
        cards.setPadding(new Insets(0));
        HBox.setHgrow(cards, Priority.ALWAYS);

        cards.getChildren().addAll(
                createMetricCard("Total", "24"),
                createMetricCard("Hoy", "8"),
                createMetricCard("Pendientes", "5"),
                createMetricCard("En reparación", "11")
        );

        content.getChildren().addAll(summaryBox, cards);
        return content;
    }

    private Node createMetricCard(String label, String value) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18, 20, 18, 20));
        card.getStyleClass().add("metric-card");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");

        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("metric-label");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }
}
