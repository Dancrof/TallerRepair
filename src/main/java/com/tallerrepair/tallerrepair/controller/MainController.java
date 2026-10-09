package com.tallerrepair.tallerrepair.controller;

import com.tallerrepair.tallerrepair.config.AppConfig;
import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.CashMovement;
import com.tallerrepair.tallerrepair.entity.CashSession;
import com.tallerrepair.tallerrepair.entity.Customer;
import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.Sale;
import com.tallerrepair.tallerrepair.entity.SaleItem;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.BudgetStatus;
import com.tallerrepair.tallerrepair.enums.CustomerType;
import com.tallerrepair.tallerrepair.enums.DeviceType;
import com.tallerrepair.tallerrepair.enums.ServiceOrderPriority;
import com.tallerrepair.tallerrepair.enums.ServiceOrderStatus;
import com.tallerrepair.tallerrepair.enums.SaleStatus;
import com.tallerrepair.tallerrepair.service.BudgetDataService;
import com.tallerrepair.tallerrepair.service.CashDataService;
import com.tallerrepair.tallerrepair.service.CustomerDataService;
import com.tallerrepair.tallerrepair.service.DashboardService;
import com.tallerrepair.tallerrepair.service.DatabaseBackupService;
import com.tallerrepair.tallerrepair.service.DeviceDataService;
import com.tallerrepair.tallerrepair.service.ProductDataService;
import com.tallerrepair.tallerrepair.service.ReportCsvExportService;
import com.tallerrepair.tallerrepair.service.SaleDataService;
import com.tallerrepair.tallerrepair.service.ServiceOrderDataService;
import com.tallerrepair.tallerrepair.service.SystemSettingsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
    private final BudgetDataService budgetDataService = new BudgetDataService();
    private final CustomerDataService customerDataService = new CustomerDataService();
    private final DeviceDataService deviceDataService = new DeviceDataService();
    private final ServiceOrderDataService serviceOrderDataService = new ServiceOrderDataService();
    private final SystemSettingsService systemSettingsService = new SystemSettingsService();
    private final DatabaseBackupService databaseBackupService = new DatabaseBackupService();
    private final ProductDataService productDataService = new ProductDataService();
    private final SaleDataService saleDataService = new SaleDataService();
    private final CashDataService cashDataService = new CashDataService();
    private final DashboardService dashboardService = new DashboardService();
    private final ReportCsvExportService reportCsvExportService = new ReportCsvExportService();
    private Button activeButton;

    @FXML
    private void initialize() {
        moduleViews.put("dashboard", createModuleView(
                "Dashboard",
                "Resumen general del taller",
                "Indicadores clave del negocio y control operativo del día."
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
        moduleViews.put("presupuestos", createModuleView(
                "Presupuestos",
                "Cotizaciones y pagos",
                "Monto total, anticipo, saldo pendiente y control de cobros."
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
        Node view;

        switch (moduleKey) {
            case "dashboard" -> view = createDashboardView();
            case "clientes" -> view = createCustomerView();
            case "equipos" -> view = createDeviceView();
            case "ordenes" -> view = createServiceOrderView();
            case "configuracion" -> view = createConfigurationView();
            case "inventario" -> view = createInventoryView();
            case "ventas" -> view = createSalesView();
            case "presupuestos" -> view = createBudgetView();
            case "caja" -> view = createCashView();
            case "reportes" -> view = createReportsView();
            default -> view = moduleViews.getOrDefault(moduleKey, createDashboardView());
        }

        contentPane.getChildren().setAll(view);

        switch (moduleKey) {
            case "dashboard" -> setHeader("Dashboard", "Resumen general del taller");
            case "clientes" -> setHeader("Clientes", "Gestión y seguimiento de clientes");
            case "equipos" -> setHeader("Equipos", "Inventario técnico por cliente");
            case "ordenes" -> setHeader("Órdenes", "Control del flujo de servicio");
            case "inventario" -> setHeader("Inventario", "Productos, costos y proveedores");
            case "ventas" -> setHeader("Ventas", "Punto de venta y comprobantes");
            case "presupuestos" -> setHeader("Presupuestos", "Cotizaciones, anticipos y saldos");
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

    private Node createDashboardView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        DashboardService.DashboardSummary summary = dashboardService.getDashboardSummary();

        VBox headerPanel = new VBox(12);
        headerPanel.getStyleClass().add("info-panel");
        Label title = new Label("Dashboard");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Indicadores clave del negocio y control operativo del día.");
        subtitle.getStyleClass().add("module-description");

        HBox quickActions = new HBox(12);
        Button newOrderButton = createActionButton("Nueva orden", "ordenes");
        Button newBudgetButton = createActionButton("Nuevo presupuesto", "presupuestos");
        Button cashButton = createActionButton("Caja", "caja");
        quickActions.getChildren().addAll(newOrderButton, newBudgetButton, cashButton);

        headerPanel.getChildren().addAll(title, subtitle, quickActions);

        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(
                createMetricCard("Ventas", formatCurrency(summary.totalSales())),
                createMetricCard("Caja", formatCurrency(summary.cashBalance())),
                createMetricCard("Stock bajo", String.valueOf(summary.lowStockProducts())),
                createMetricCard("Presupuestos", String.valueOf(summary.budgetCount()))
        );

        VBox salesPanel = new VBox(12);
        salesPanel.getStyleClass().add("info-panel");
        Label salesTitle = new Label("Ventas recientes");
        salesTitle.getStyleClass().add("module-summary");

        TableView<DashboardRow> salesTable = new TableView<>();
        salesTable.setPlaceholder(new Label("No hay ventas registradas."));
        salesTable.setItems(createDashboardRows());

        TableColumn<DashboardRow, String> codeColumn = new TableColumn<>("Venta");
        codeColumn.setCellValueFactory(cell -> cell.getValue().codeProperty());
        TableColumn<DashboardRow, String> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(cell -> cell.getValue().totalProperty());
        TableColumn<DashboardRow, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        salesTable.getColumns().addAll(codeColumn, totalColumn, statusColumn);
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        salesPanel.getChildren().addAll(salesTitle, salesTable);

        root.getChildren().addAll(headerPanel, metrics, salesPanel);
        return root;
    }

    private Node createReportsView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));

        VBox header = new VBox(10);
        header.getStyleClass().add("info-panel");
        Label title = new Label("Reportes");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Consulta los movimientos del taller y exporta el resultado filtrado.");
        subtitle.getStyleClass().add("module-description");

        ComboBox<String> reportType = new ComboBox<>();
        reportType.getItems().addAll("Ventas", "Presupuestos", "Caja", "Inventario");
        reportType.setValue("Ventas");
        reportType.setPrefWidth(160);
        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("Desde");
        DatePicker toDate = new DatePicker();
        toDate.setPromptText("Hasta");
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar referencia, cliente o detalle");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.setPrefWidth(170);
        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("secondary-button");
        Button exportButton = new Button("Exportar CSV");
        exportButton.getStyleClass().add("primary-button");
        HBox toolbar = new HBox(10, reportType, fromDate, toDate, statusFilter, searchField, clearButton, exportButton);
        toolbar.getStyleClass().add("filter-toolbar");
        header.getChildren().addAll(title, subtitle, toolbar);

        ObservableList<ReportRow> reportRows = FXCollections.observableArrayList(buildReportRows("Ventas"));
        FilteredList<ReportRow> filteredRows = new FilteredList<>(reportRows);
        Label countValue = new Label("0");
        Label totalValue = new Label(formatCurrency(BigDecimal.ZERO));
        Label periodValue = new Label("Todos los periodos");
        HBox summary = new HBox(14,
                createReportStatCard("Registros", countValue),
                createReportStatCard("Importe neto", totalValue),
                createReportStatCard("Periodo", periodValue));

        TableView<ReportRow> table = new TableView<>(filteredRows);
        table.setPlaceholder(new Label("No hay registros para los filtros seleccionados."));
        TableColumn<ReportRow, String> dateColumn = new TableColumn<>("Fecha");
        dateColumn.setCellValueFactory(cell -> cell.getValue().dateProperty());
        TableColumn<ReportRow, String> typeColumn = new TableColumn<>("Tipo");
        typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
        TableColumn<ReportRow, String> referenceColumn = new TableColumn<>("Referencia");
        referenceColumn.setCellValueFactory(cell -> cell.getValue().referenceProperty());
        TableColumn<ReportRow, String> subjectColumn = new TableColumn<>("Cliente / detalle");
        subjectColumn.setCellValueFactory(cell -> cell.getValue().subjectProperty());
        TableColumn<ReportRow, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        TableColumn<ReportRow, String> amountColumn = new TableColumn<>("Importe");
        amountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty());
        table.getColumns().addAll(dateColumn, typeColumn, referenceColumn, subjectColumn, statusColumn, amountColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable refreshStatuses = () -> {
            List<String> statuses = reportRows.stream().map(row -> row.statusProperty().get()).distinct().sorted().toList();
            statusFilter.getItems().setAll("Todos");
            statusFilter.getItems().addAll(statuses);
            statusFilter.setValue("Todos");
        };
        Runnable updateFilter = () -> {
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            String selectedStatus = statusFilter.getValue();
            String query = normalizeText(searchField.getText());
            filteredRows.setPredicate(row -> {
                boolean matchesDate = "Inventario".equals(reportType.getValue()) || row.dateValue() == null
                        || ((from == null || !row.dateValue().toLocalDate().isBefore(from))
                        && (to == null || !row.dateValue().toLocalDate().isAfter(to)));
                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Todos")
                        || selectedStatus.equals(row.statusProperty().get());
                boolean matchesText = query.isBlank() || normalizeText(row.searchText()).contains(query);
                return matchesDate && matchesStatus && matchesText;
            });
            BigDecimal total = filteredRows.stream().map(ReportRow::amountValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            countValue.setText(String.valueOf(filteredRows.size()));
            totalValue.setText(formatCurrency(total));
            if ("Inventario".equals(reportType.getValue())) {
                periodValue.setText("Stock actual");
            } else if (from == null && to == null) {
                periodValue.setText("Todos los periodos");
            } else {
                periodValue.setText((from == null ? "Inicio" : from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        + " – " + (to == null ? "Hoy" : to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            }
        };

        reportType.valueProperty().addListener((observable, previous, selected) -> {
            reportRows.setAll(buildReportRows(selected));
            boolean isInventory = "Inventario".equals(selected);
            fromDate.setDisable(isInventory);
            toDate.setDisable(isInventory);
            refreshStatuses.run();
            updateFilter.run();
        });
        statusFilter.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        fromDate.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        toDate.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        searchField.textProperty().addListener((observable, previous, current) -> updateFilter.run());
        clearButton.setOnAction(event -> {
            fromDate.setValue(null);
            toDate.setValue(null);
            searchField.clear();
            statusFilter.setValue("Todos");
            updateFilter.run();
        });
        exportButton.setOnAction(event -> exportReport(reportType.getValue(), filteredRows));

        refreshStatuses.run();
        updateFilter.run();
        root.getChildren().addAll(header, summary, table);
        return root;
    }

    private List<ReportRow> buildReportRows(String reportType) {
        List<ReportRow> rows = new java.util.ArrayList<>();
        switch (reportType) {
            case "Ventas" -> {
                for (Sale sale : saleDataService.getAllSales()) {
                    String products = sale.getItems().stream()
                            .map(item -> item.getProduct() == null ? "Producto" : item.getProduct().getName())
                            .distinct().reduce((left, right) -> left + ", " + right).orElse("Sin líneas");
                    String status = sale.getStatus() == SaleStatus.PAID ? "Pagada"
                            : sale.getStatus() == SaleStatus.CANCELLED ? "Cancelada" : "Pendiente";
                    rows.add(new ReportRow(sale.getSaleDate(), "Venta", sale.getSaleNumber(), products,
                            status, sale.getTotal()));
                }
            }
            case "Presupuestos" -> {
                for (Budget budget : budgetDataService.getAllBudgets()) {
                    rows.add(new ReportRow(budget.getIssuedAt(), "Presupuesto", budget.getBudgetNumber(),
                            resolveCustomerName(budget), getBudgetStatusLabel(budget.getStatus()), budget.getTotal()));
                }
            }
            case "Caja" -> {
                for (CashSession session : cashDataService.getAllSessionsWithMovements()) {
                    for (CashMovement movement : session.getMovements()) {
                        String status = switch (movement.getMovementType()) {
                            case SALE, OPENING -> "Entrada";
                            case REFUND, EXPENSE -> "Salida";
                            default -> "Otro";
                        };
                        BigDecimal amount = movement.getAmount() == null ? BigDecimal.ZERO : movement.getAmount();
                        if ("Salida".equals(status)) {
                            amount = amount.negate();
                        }
                        rows.add(new ReportRow(movement.getMovementAt(), "Caja", session.getSessionNumber(),
                                movement.getDescription(), status, amount));
                    }
                }
            }
            case "Inventario" -> {
                for (Product product : productDataService.getProducts()) {
                    BigDecimal stockValue = product.getSalePrice()
                            .multiply(BigDecimal.valueOf(product.getQuantityOnHand()));
                    rows.add(new ReportRow(null, product.getCategory(), product.getSku(), product.getName(),
                            product.isLowStock() ? "Stock bajo" : "Disponible", stockValue));
                }
            }
            default -> throw new IllegalArgumentException("Tipo de reporte no válido.");
        }
        return rows;
    }

    private VBox createReportStatCard(String title, Label value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("detail-field");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-label");
        value.getStyleClass().add("detail-value");
        card.getChildren().addAll(titleLabel, value);
        return card;
    }

    private void exportReport(String reportType, List<ReportRow> rows) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar reporte CSV");
        chooser.setInitialFileName("tallerrepair-" + reportType.toLowerCase(java.util.Locale.ROOT) + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV", "*.csv"));
        java.io.File destination = chooser.showSaveDialog(contentPane.getScene().getWindow());
        if (destination == null) {
            return;
        }
        try {
            List<List<String>> values = rows.stream().map(ReportRow::csvValues).toList();
            reportCsvExportService.export(destination.toPath(),
                    List.of("Fecha", "Tipo", "Referencia", "Cliente o detalle", "Estado", "Importe"), values);
            showMessage(Alert.AlertType.INFORMATION, "Reporte exportado", destination.getAbsolutePath());
        } catch (IOException exception) {
            showMessage(Alert.AlertType.ERROR, "No se pudo exportar", exception.getMessage());
        }
    }

    private Node createConfigurationView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));

        VBox header = new VBox(8);
        header.getStyleClass().add("info-panel");
        Label title = new Label("Configuración del taller");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Administra los datos de identificación y protege la base de datos local.");
        subtitle.getStyleClass().add("module-description");
        header.getChildren().addAll(title, subtitle);

        SystemSettingsService.CompanyProfile profile = systemSettingsService.getCompanyProfile();
        VBox companyPanel = new VBox(12);
        companyPanel.getStyleClass().add("info-panel");
        Label companyTitle = new Label("Datos del taller");
        companyTitle.getStyleClass().add("module-summary");
        TextField nameField = new TextField(valueOrEmpty(profile.name()));
        TextField taxIdField = new TextField(valueOrEmpty(profile.taxId()));
        TextField phoneField = new TextField(valueOrEmpty(profile.phone()));
        TextField emailField = new TextField(valueOrEmpty(profile.email()));
        TextArea addressField = new TextArea(valueOrEmpty(profile.address()));
        addressField.setPrefRowCount(2);

        GridPane companyForm = new GridPane();
        companyForm.setHgap(14);
        companyForm.setVgap(10);
        addSettingsField(companyForm, "Nombre comercial", nameField, 0);
        addSettingsField(companyForm, "Identificación fiscal", taxIdField, 1);
        addSettingsField(companyForm, "Teléfono", phoneField, 2);
        addSettingsField(companyForm, "Correo", emailField, 3);
        addSettingsField(companyForm, "Dirección", addressField, 4);

        Label saveResult = new Label();
        saveResult.getStyleClass().add("module-description");
        Button saveButton = new Button("Guardar datos del taller");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setOnAction(event -> {
            try {
                systemSettingsService.saveCompanyProfile(new SystemSettingsService.CompanyProfile(
                        nameField.getText(), taxIdField.getText(), phoneField.getText(),
                        emailField.getText(), addressField.getText()));
                saveResult.setText("Datos guardados.");
            } catch (IllegalArgumentException exception) {
                saveResult.setText(exception.getMessage());
            }
        });
        companyPanel.getChildren().addAll(companyTitle, companyForm, new HBox(12, saveButton, saveResult));

        VBox backupPanel = new VBox(12);
        backupPanel.getStyleClass().add("info-panel");
        Label backupTitle = new Label("Base de datos y copias de seguridad");
        backupTitle.getStyleClass().add("module-summary");
        Path databasePath = AppConfig.APP_HOME.resolve("tallerrepair.db");
        String databaseSize;
        try {
            databaseSize = Files.exists(databasePath) ? formatFileSize(Files.size(databasePath)) : "No encontrada";
        } catch (IOException exception) {
            databaseSize = "No disponible";
        }

        GridPane databaseInfo = new GridPane();
        databaseInfo.setHgap(14);
        databaseInfo.setVgap(8);
        Label pathValue = new Label(databasePath.toAbsolutePath().toString());
        pathValue.setWrapText(true);
        addCustomerDetail(databaseInfo, 0, "Archivo de datos", pathValue);
        addCustomerDetail(databaseInfo, 1, "Tamaño actual", new Label(databaseSize));
        Label backupDirectory = new Label(AppConfig.BACKUPS_DIR.toAbsolutePath().toString());
        backupDirectory.setWrapText(true);
        addCustomerDetail(databaseInfo, 2, "Carpeta de copias", backupDirectory);

        Button createBackupButton = new Button("Crear copia ahora");
        createBackupButton.getStyleClass().add("primary-button");
        createBackupButton.setOnAction(event -> {
            try {
                Path backup = databaseBackupService.createBackup();
                showMessage(Alert.AlertType.INFORMATION, "Copia creada", backup.toAbsolutePath().toString());
                renderModule("configuracion");
            } catch (IllegalStateException exception) {
                showMessage(Alert.AlertType.ERROR, "No se pudo crear la copia", exception.getMessage());
            }
        });

        TableView<BackupRow> backupsTable = new TableView<>();
        backupsTable.setPlaceholder(new Label("Todavía no hay copias de seguridad."));
        backupsTable.setItems(FXCollections.observableArrayList(getBackupRows()));
        TableColumn<BackupRow, String> backupNameColumn = new TableColumn<>("Archivo");
        backupNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        TableColumn<BackupRow, String> backupDateColumn = new TableColumn<>("Creada");
        backupDateColumn.setCellValueFactory(cell -> cell.getValue().dateProperty());
        TableColumn<BackupRow, String> backupSizeColumn = new TableColumn<>("Tamaño");
        backupSizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        backupsTable.getColumns().addAll(backupNameColumn, backupDateColumn, backupSizeColumn);
        backupsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        backupPanel.getChildren().addAll(backupTitle, databaseInfo, createBackupButton, backupsTable);

        root.getChildren().addAll(header, companyPanel, backupPanel);
        return root;
    }

    private void addSettingsField(GridPane form, String label, Node control, int row) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("metric-label");
        form.add(fieldLabel, 0, row);
        form.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
        control.setStyle("-fx-pref-width: 420px;");
    }

    private List<BackupRow> getBackupRows() {
        List<BackupRow> rows = new java.util.ArrayList<>();
        try (var backups = Files.list(AppConfig.BACKUPS_DIR)) {
            for (Path path : backups.filter(Files::isRegularFile).sorted(java.util.Comparator.reverseOrder()).toList()) {
                try {
                    rows.add(new BackupRow(path, Files.getLastModifiedTime(path).toMillis(), Files.size(path)));
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
            return rows;
        }
        return rows;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private Node createServiceOrderView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));

        List<ServiceOrder> orders = serviceOrderDataService.getOrders();
        ObservableList<ServiceOrderRow> rows = FXCollections.observableArrayList(
                orders.stream().map(ServiceOrderRow::new).toList()
        );
        FilteredList<ServiceOrderRow> filteredRows = new FilteredList<>(rows);

        VBox header = new VBox(12);
        header.getStyleClass().add("info-panel");
        Label title = new Label("Órdenes de servicio");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Recepción, diagnóstico, reparación y entrega de equipos.");
        subtitle.getStyleClass().add("module-description");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar orden, cliente, equipo o avería");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().add("Todos los estados");
        for (ServiceOrderStatus status : ServiceOrderStatus.values()) {
            statusFilter.getItems().add(serviceOrderStatusLabel(status));
        }
        statusFilter.setValue("Todos los estados");

        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("secondary-button");
        Button newOrderButton = new Button("Nueva orden");
        newOrderButton.getStyleClass().add("primary-button");
        newOrderButton.setOnAction(event -> showServiceOrderDialog());
        HBox toolbar = new HBox(10, searchField, statusFilter, clearButton, newOrderButton);
        toolbar.getStyleClass().add("filter-toolbar");
        header.getChildren().addAll(title, subtitle, toolbar);

        Runnable updateFilter = () -> {
            String query = normalizeText(searchField.getText());
            String selectedStatus = statusFilter.getValue();
            filteredRows.setPredicate(row -> {
                boolean matchesText = query.isBlank() || normalizeText(row.searchText()).contains(query);
                boolean matchesStatus = selectedStatus == null || selectedStatus.equals("Todos los estados")
                        || selectedStatus.equals(row.statusProperty().get());
                return matchesText && matchesStatus;
            });
        };
        searchField.textProperty().addListener((observable, previous, current) -> updateFilter.run());
        statusFilter.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            statusFilter.setValue("Todos los estados");
            updateFilter.run();
        });

        TableView<ServiceOrderRow> table = new TableView<>(filteredRows);
        table.setPlaceholder(new Label("No hay órdenes registradas. Usa “Nueva orden” para recibir un equipo."));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TableColumn<ServiceOrderRow, String> numberColumn = new TableColumn<>("Orden");
        numberColumn.setCellValueFactory(cell -> cell.getValue().numberProperty());
        TableColumn<ServiceOrderRow, String> customerColumn = new TableColumn<>("Cliente");
        customerColumn.setCellValueFactory(cell -> cell.getValue().customerProperty());
        TableColumn<ServiceOrderRow, String> deviceColumn = new TableColumn<>("Equipo");
        deviceColumn.setCellValueFactory(cell -> cell.getValue().deviceProperty());
        TableColumn<ServiceOrderRow, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        TableColumn<ServiceOrderRow, String> priorityColumn = new TableColumn<>("Prioridad");
        priorityColumn.setCellValueFactory(cell -> cell.getValue().priorityProperty());
        TableColumn<ServiceOrderRow, String> receivedColumn = new TableColumn<>("Recibido");
        receivedColumn.setCellValueFactory(cell -> cell.getValue().receivedProperty());
        TableColumn<ServiceOrderRow, String> balanceColumn = new TableColumn<>("Saldo");
        balanceColumn.setCellValueFactory(cell -> cell.getValue().balanceProperty());
        table.getColumns().addAll(numberColumn, customerColumn, deviceColumn, statusColumn,
                priorityColumn, receivedColumn, balanceColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox detail = new VBox(12);
        detail.getStyleClass().add("customer-detail-panel");
        Label detailTitle = new Label("Detalle de la orden");
        detailTitle.getStyleClass().add("module-summary");
        Label detailNumber = new Label("Selecciona una orden para consultar y actualizar su estado.");
        detailNumber.getStyleClass().add("detail-value");
        GridPane details = new GridPane();
        details.setHgap(28);
        details.setVgap(10);
        Label customerValue = new Label("-");
        Label deviceValue = new Label("-");
        Label failureValue = new Label("-");
        Label diagnosisValue = new Label("-");
        Label workValue = new Label("-");
        Label deliveryValue = new Label("-");
        Label totalValue = new Label("-");
        addCustomerDetail(details, 0, "Cliente", customerValue);
        addCustomerDetail(details, 1, "Equipo", deviceValue);
        addCustomerDetail(details, 2, "Falla declarada", failureValue);
        addCustomerDetail(details, 3, "Diagnóstico", diagnosisValue);
        addCustomerDetail(details, 4, "Trabajo realizado", workValue);
        addCustomerDetail(details, 5, "Entrega estimada", deliveryValue);
        addCustomerDetail(details, 6, "Total · saldo", totalValue);

        ComboBox<ServiceOrderStatus> statusField = new ComboBox<>(
                FXCollections.observableArrayList(ServiceOrderStatus.values()));
        statusField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ServiceOrderStatus status) {
                return status == null ? "" : serviceOrderStatusLabel(status);
            }

            @Override
            public ServiceOrderStatus fromString(String value) {
                return java.util.Arrays.stream(ServiceOrderStatus.values())
                        .filter(status -> serviceOrderStatusLabel(status).equals(value)).findFirst().orElse(null);
            }
        });
        TextArea statusNotes = new TextArea();
        statusNotes.setPromptText("Nota para el historial del cambio de estado");
        statusNotes.setPrefRowCount(2);
        Button changeStatusButton = new Button("Guardar estado");
        changeStatusButton.getStyleClass().add("primary-button");
        changeStatusButton.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            boolean hasSelection = selected != null;
            statusField.setDisable(!hasSelection);
            statusNotes.setDisable(!hasSelection);
            changeStatusButton.setDisable(!hasSelection);
            if (hasSelection) {
                ServiceOrder order = selected.order();
                detailNumber.setText(order.getOrderNumber());
                customerValue.setText(customerDisplayName(order.getCustomer()));
                deviceValue.setText(deviceDisplayName(order.getDevice()));
                failureValue.setText(displayValue(order.getDeclaredFailure()));
                diagnosisValue.setText(displayValue(order.getDiagnosis()));
                workValue.setText(displayValue(order.getWorkPerformed()));
                deliveryValue.setText(order.getEstimatedDeliveryAt() == null
                        ? "Sin fecha" : order.getEstimatedDeliveryAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                totalValue.setText(formatCurrency(order.getTotal()) + " · " + formatCurrency(order.getBalance()));
                statusField.setValue(order.getStatus());
                statusNotes.clear();
            } else {
                detailNumber.setText("Selecciona una orden para consultar y actualizar su estado.");
                customerValue.setText("-");
                deviceValue.setText("-");
                failureValue.setText("-");
                diagnosisValue.setText("-");
                workValue.setText("-");
                deliveryValue.setText("-");
                totalValue.setText("-");
                statusField.setValue(null);
                statusNotes.clear();
            }
        });
        changeStatusButton.setOnAction(event -> {
            ServiceOrderRow selected = table.getSelectionModel().getSelectedItem();
            if (selected == null || statusField.getValue() == null) {
                return;
            }
            try {
                serviceOrderDataService.changeStatus(selected.order(), statusField.getValue(), statusNotes.getText());
                renderModule("ordenes");
            } catch (IllegalArgumentException | IllegalStateException exception) {
                showMessage(Alert.AlertType.ERROR, "No se pudo cambiar el estado", exception.getMessage());
            }
        });

        HBox statusActions = new HBox(10, statusField, changeStatusButton);
        detail.getChildren().addAll(detailTitle, detailNumber, details, statusActions, statusNotes);
        root.getChildren().addAll(header, table, detail);
        return root;
    }

    private void showServiceOrderDialog() {
        List<Customer> customers = customerDataService.getActiveCustomers();
        List<Device> devices = deviceDataService.getDevices();
        if (customers.isEmpty()) {
            showMessage(Alert.AlertType.INFORMATION, "No hay clientes", "Registra primero un cliente antes de crear una orden.");
            return;
        }
        if (devices.isEmpty()) {
            showMessage(Alert.AlertType.INFORMATION, "No hay equipos", "Registra primero un equipo y asígnalo a un cliente.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nueva orden de servicio");
        dialog.setHeaderText("Recibe el equipo y registra la falla indicada.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        ComboBox<Customer> customerField = new ComboBox<>(FXCollections.observableArrayList(customers));
        customerField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customerDisplayName(customer);
            }

            @Override
            public Customer fromString(String value) {
                return customers.stream().filter(customer -> customerDisplayName(customer).equals(value))
                        .findFirst().orElse(null);
            }
        });
        ComboBox<Device> deviceField = new ComboBox<>();
        deviceField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Device device) {
                return device == null ? "" : deviceDisplayName(device);
            }

            @Override
            public Device fromString(String value) {
                return devices.stream().filter(device -> deviceDisplayName(device).equals(value))
                        .findFirst().orElse(null);
            }
        });
        Runnable updateDeviceChoices = () -> {
            Customer selectedCustomer = customerField.getValue();
            List<Device> customerDevices = selectedCustomer == null ? List.of() : devices.stream()
                    .filter(device -> device.getCustomer().getId().equals(selectedCustomer.getId())).toList();
            deviceField.setItems(FXCollections.observableArrayList(customerDevices));
            deviceField.setValue(customerDevices.isEmpty() ? null : customerDevices.get(0));
        };
        customerField.valueProperty().addListener((observable, previous, selected) -> updateDeviceChoices.run());
        customerField.setValue(customers.get(0));
        updateDeviceChoices.run();

        ComboBox<ServiceOrderPriority> priorityField = new ComboBox<>(
                FXCollections.observableArrayList(ServiceOrderPriority.values()));
        priorityField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ServiceOrderPriority priority) {
                return priority == null ? "" : serviceOrderPriorityLabel(priority);
            }

            @Override
            public ServiceOrderPriority fromString(String value) {
                return java.util.Arrays.stream(ServiceOrderPriority.values())
                        .filter(priority -> serviceOrderPriorityLabel(priority).equals(value)).findFirst().orElse(null);
            }
        });
        priorityField.setValue(ServiceOrderPriority.NORMAL);
        TextArea failureField = new TextArea();
        failureField.setPromptText("Describe el problema que indica el cliente");
        failureField.setPrefRowCount(3);
        DatePicker deliveryField = new DatePicker(LocalDate.now().plusDays(5));
        TextField estimatedCostField = new TextField("0.00");
        TextArea notesField = new TextArea();
        notesField.setPrefRowCount(2);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(9);
        form.setPadding(new Insets(10, 4, 4, 4));
        addDeviceFormField(form, "Cliente", customerField, 0);
        addDeviceFormField(form, "Equipo", deviceField, 1);
        addDeviceFormField(form, "Prioridad", priorityField, 2);
        addDeviceFormField(form, "Entrega estimada", deliveryField, 3);
        addDeviceFormField(form, "Costo estimado (€)", estimatedCostField, 4);
        addDeviceFormField(form, "Falla declarada", failureField, 5);
        addDeviceFormField(form, "Notas", notesField, 6);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(580);

        while (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            try {
                ServiceOrder order = new ServiceOrder();
                order.setCustomer(customerField.getValue());
                order.setDevice(deviceField.getValue());
                order.setPriority(priorityField.getValue());
                order.setReceivedAt(java.time.LocalDateTime.now());
                order.setEstimatedDeliveryAt(deliveryField.getValue());
                order.setDeclaredFailure(failureField.getText());
                order.setEstimatedCost(new BigDecimal(estimatedCostField.getText().trim().replace(',', '.')));
                order.setNotes(notesField.getText());
                serviceOrderDataService.create(order);
                renderModule("ordenes");
                return;
            } catch (IllegalArgumentException exception) {
                showMessage(Alert.AlertType.ERROR, "No se pudo crear la orden", exception.getMessage());
            }
        }
    }

    private String serviceOrderStatusLabel(ServiceOrderStatus status) {
        return switch (status) {
            case RECEIVED -> "Recibida";
            case DIAGNOSIS -> "Diagnóstico";
            case QUOTED -> "Presupuestada";
            case APPROVED -> "Aprobada";
            case IN_REPAIR -> "En reparación";
            case WAITING_PARTS -> "Esperando repuestos";
            case READY -> "Lista para entrega";
            case DELIVERED -> "Entregada";
            case CANCELLED -> "Cancelada";
        };
    }

    private String serviceOrderPriorityLabel(ServiceOrderPriority priority) {
        return switch (priority) {
            case LOW -> "Baja";
            case NORMAL -> "Normal";
            case HIGH -> "Alta";
            case URGENT -> "Urgente";
        };
    }

    private String deviceDisplayName(Device device) {
        if (device == null) {
            return "";
        }
        String brand = valueOrEmpty(device.getBrand());
        String model = valueOrEmpty(device.getModel());
        String name = (brand + " " + model).trim();
        return name.isBlank() ? deviceTypeLabel(device.getDeviceType()) : name;
    }

    private Node createCustomerView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));

        List<Customer> customers = customerDataService.getActiveCustomers();
        ObservableList<CustomerRow> customerRows = FXCollections.observableArrayList(
                customers.stream().map(CustomerRow::new).toList()
        );
        FilteredList<CustomerRow> filteredRows = new FilteredList<>(customerRows);

        VBox header = new VBox(12);
        header.getStyleClass().add("info-panel");

        Label title = new Label("Clientes");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Consulta y administra los datos de contacto de tus clientes.");
        subtitle.getStyleClass().add("module-description");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre, documento, teléfono o correo");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Todos", "Persona", "Empresa");
        typeFilter.setValue("Todos");
        typeFilter.setPrefWidth(150);

        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("secondary-button");

        Button newCustomerButton = new Button("Nuevo cliente");
        newCustomerButton.getStyleClass().add("primary-button");
        newCustomerButton.setOnAction(event -> showCustomerDialog(null));

        HBox toolbar = new HBox(10, searchField, typeFilter, clearButton, newCustomerButton);
        toolbar.getStyleClass().add("filter-toolbar");
        header.getChildren().addAll(title, subtitle, toolbar);

        Runnable updateFilter = () -> {
            String query = normalizeText(searchField.getText());
            String selectedType = typeFilter.getValue();
            filteredRows.setPredicate(row -> {
                boolean matchesText = query.isBlank() || normalizeText(row.searchText()).contains(query);
                boolean matchesType = selectedType == null || selectedType.equals("Todos")
                        || selectedType.equals(row.typeProperty().get());
                return matchesText && matchesType;
            });
        };
        searchField.textProperty().addListener((observable, previous, current) -> updateFilter.run());
        typeFilter.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            typeFilter.setValue("Todos");
            updateFilter.run();
        });

        TableView<CustomerRow> table = new TableView<>(filteredRows);
        table.setPlaceholder(new Label("No hay clientes activos. Usa “Nuevo cliente” para registrar el primero."));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<CustomerRow, String> nameColumn = new TableColumn<>("Cliente");
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        TableColumn<CustomerRow, String> typeColumn = new TableColumn<>("Tipo");
        typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
        TableColumn<CustomerRow, String> documentColumn = new TableColumn<>("Documento");
        documentColumn.setCellValueFactory(cell -> cell.getValue().documentProperty());
        TableColumn<CustomerRow, String> phoneColumn = new TableColumn<>("Teléfono");
        phoneColumn.setCellValueFactory(cell -> cell.getValue().phoneProperty());
        TableColumn<CustomerRow, String> emailColumn = new TableColumn<>("Correo electrónico");
        emailColumn.setCellValueFactory(cell -> cell.getValue().emailProperty());
        TableColumn<CustomerRow, String> locationColumn = new TableColumn<>("Localidad");
        locationColumn.setCellValueFactory(cell -> cell.getValue().locationProperty());
        table.getColumns().addAll(nameColumn, typeColumn, documentColumn, phoneColumn, emailColumn, locationColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox detail = new VBox(12);
        detail.getStyleClass().add("customer-detail-panel");
        Label detailTitle = new Label("Ficha del cliente");
        detailTitle.getStyleClass().add("module-summary");
        Label detailName = new Label("Selecciona un cliente para ver su ficha.");
        detailName.getStyleClass().add("detail-value");
        GridPane detailGrid = new GridPane();
        detailGrid.setHgap(28);
        detailGrid.setVgap(10);
        Label documentValue = new Label("-");
        Label phoneValue = new Label("-");
        Label emailValue = new Label("-");
        Label locationValue = new Label("-");
        Label addressValue = new Label("-");
        Label notesValue = new Label("-");
        addCustomerDetail(detailGrid, 0, "Documento", documentValue);
        addCustomerDetail(detailGrid, 1, "Teléfono", phoneValue);
        addCustomerDetail(detailGrid, 2, "Correo", emailValue);
        addCustomerDetail(detailGrid, 3, "Localidad", locationValue);
        addCustomerDetail(detailGrid, 4, "Dirección", addressValue);
        addCustomerDetail(detailGrid, 5, "Notas", notesValue);

        Button editButton = new Button("Editar");
        editButton.getStyleClass().add("secondary-button");
        Button deactivateButton = new Button("Desactivar");
        deactivateButton.getStyleClass().add("secondary-button");
        editButton.setDisable(true);
        deactivateButton.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            boolean hasSelection = selected != null;
            editButton.setDisable(!hasSelection);
            deactivateButton.setDisable(!hasSelection);
            if (hasSelection) {
                Customer customer = selected.customer();
                detailName.setText(selected.nameProperty().get());
                documentValue.setText(displayValue(customer.getDocument()));
                phoneValue.setText(displayValue(customer.getPhone()));
                emailValue.setText(displayValue(customer.getEmail()));
                locationValue.setText(displayLocation(customer));
                addressValue.setText(displayValue(customer.getAddress()));
                notesValue.setText(displayValue(customer.getNotes()));
            } else {
                detailName.setText("Selecciona un cliente para ver su ficha.");
                documentValue.setText("-");
                phoneValue.setText("-");
                emailValue.setText("-");
                locationValue.setText("-");
                addressValue.setText("-");
                notesValue.setText("-");
            }
        });

        editButton.setOnAction(event -> {
            CustomerRow selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCustomerDialog(selected.customer());
            }
        });
        deactivateButton.setOnAction(event -> {
            CustomerRow selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Desactivar a " + selected.nameProperty().get() + "? Ya no aparecerá en el listado activo.",
                    ButtonType.CANCEL, ButtonType.OK);
            confirmation.setHeaderText("Desactivar cliente");
            confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(button -> {
                customerDataService.deactivate(selected.customer());
                renderModule("clientes");
            });
        });

        HBox detailActions = new HBox(10, editButton, deactivateButton);
        detail.getChildren().addAll(detailTitle, detailName, detailGrid, detailActions);
        root.getChildren().addAll(header, table, detail);
        return root;
    }

    private Node createDeviceView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));

        List<Device> devices = deviceDataService.getDevices();
        ObservableList<DeviceRow> rows = FXCollections.observableArrayList(
                devices.stream().map(DeviceRow::new).toList()
        );
        FilteredList<DeviceRow> filteredRows = new FilteredList<>(rows);

        VBox header = new VBox(12);
        header.getStyleClass().add("info-panel");
        Label title = new Label("Equipos");
        title.getStyleClass().add("module-title");
        Label subtitle = new Label("Equipos registrados por cliente, con identificación y datos de recepción.");
        subtitle.getStyleClass().add("module-description");

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar cliente, marca, modelo, serie o IMEI");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("Todos los tipos");
        for (DeviceType type : DeviceType.values()) {
            typeFilter.getItems().add(deviceTypeLabel(type));
        }
        typeFilter.setValue("Todos los tipos");
        typeFilter.setPrefWidth(180);

        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("secondary-button");
        Button newDeviceButton = new Button("Nuevo equipo");
        newDeviceButton.getStyleClass().add("primary-button");
        newDeviceButton.setOnAction(event -> showDeviceDialog(null));

        HBox toolbar = new HBox(10, searchField, typeFilter, clearButton, newDeviceButton);
        toolbar.getStyleClass().add("filter-toolbar");
        header.getChildren().addAll(title, subtitle, toolbar);

        Runnable updateFilter = () -> {
            String query = normalizeText(searchField.getText());
            String selectedType = typeFilter.getValue();
            filteredRows.setPredicate(row -> {
                boolean matchesText = query.isBlank() || normalizeText(row.searchText()).contains(query);
                boolean matchesType = selectedType == null || selectedType.equals("Todos los tipos")
                        || selectedType.equals(row.typeProperty().get());
                return matchesText && matchesType;
            });
        };
        searchField.textProperty().addListener((observable, previous, current) -> updateFilter.run());
        typeFilter.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            typeFilter.setValue("Todos los tipos");
            updateFilter.run();
        });

        TableView<DeviceRow> table = new TableView<>(filteredRows);
        table.setPlaceholder(new Label("No hay equipos registrados. Usa “Nuevo equipo” para asociar el primero a un cliente."));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<DeviceRow, String> ownerColumn = new TableColumn<>("Cliente");
        ownerColumn.setCellValueFactory(cell -> cell.getValue().customerProperty());
        TableColumn<DeviceRow, String> typeColumn = new TableColumn<>("Tipo");
        typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());
        TableColumn<DeviceRow, String> modelColumn = new TableColumn<>("Equipo");
        modelColumn.setCellValueFactory(cell -> cell.getValue().modelProperty());
        TableColumn<DeviceRow, String> serialColumn = new TableColumn<>("N° de serie");
        serialColumn.setCellValueFactory(cell -> cell.getValue().serialProperty());
        TableColumn<DeviceRow, String> imeiColumn = new TableColumn<>("IMEI");
        imeiColumn.setCellValueFactory(cell -> cell.getValue().imeiProperty());
        table.getColumns().addAll(ownerColumn, typeColumn, modelColumn, serialColumn, imeiColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox detail = new VBox(12);
        detail.getStyleClass().add("customer-detail-panel");
        Label detailTitle = new Label("Ficha del equipo");
        detailTitle.getStyleClass().add("module-summary");
        Label detailName = new Label("Selecciona un equipo para consultar su ficha.");
        detailName.getStyleClass().add("detail-value");
        GridPane detailGrid = new GridPane();
        detailGrid.setHgap(28);
        detailGrid.setVgap(10);
        Label ownerValue = new Label("-");
        Label serialValue = new Label("-");
        Label imeiValue = new Label("-");
        Label colorValue = new Label("-");
        Label accessoriesValue = new Label("-");
        Label conditionValue = new Label("-");
        Label notesValue = new Label("-");
        addCustomerDetail(detailGrid, 0, "Cliente", ownerValue);
        addCustomerDetail(detailGrid, 1, "Número de serie", serialValue);
        addCustomerDetail(detailGrid, 2, "IMEI", imeiValue);
        addCustomerDetail(detailGrid, 3, "Color", colorValue);
        addCustomerDetail(detailGrid, 4, "Accesorios", accessoriesValue);
        addCustomerDetail(detailGrid, 5, "Estado físico", conditionValue);
        addCustomerDetail(detailGrid, 6, "Notas", notesValue);

        Button editButton = new Button("Editar equipo");
        editButton.getStyleClass().add("secondary-button");
        editButton.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) -> {
            boolean hasSelection = selected != null;
            editButton.setDisable(!hasSelection);
            if (hasSelection) {
                Device device = selected.device();
                detailName.setText(selected.modelProperty().get());
                ownerValue.setText(selected.customerProperty().get());
                serialValue.setText(displayValue(device.getSerialNumber()));
                imeiValue.setText(displayValue(device.getImei()));
                colorValue.setText(displayValue(device.getColor()));
                accessoriesValue.setText(displayValue(device.getAccessories()));
                conditionValue.setText(displayValue(device.getPhysicalCondition()));
                notesValue.setText(displayValue(device.getNotes()));
            } else {
                detailName.setText("Selecciona un equipo para consultar su ficha.");
                ownerValue.setText("-");
                serialValue.setText("-");
                imeiValue.setText("-");
                colorValue.setText("-");
                accessoriesValue.setText("-");
                conditionValue.setText("-");
                notesValue.setText("-");
            }
        });
        editButton.setOnAction(event -> {
            DeviceRow selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeviceDialog(selected.device());
            }
        });

        HBox detailActions = new HBox(10, editButton, createActionButton("Ir a Clientes", "clientes"));
        detail.getChildren().addAll(detailTitle, detailName, detailGrid, detailActions);
        root.getChildren().addAll(header, table, detail);
        return root;
    }

    private void showDeviceDialog(Device existingDevice) {
        List<Customer> customers = customerDataService.getActiveCustomers();
        if (customers.isEmpty()) {
            showMessage(Alert.AlertType.INFORMATION, "No hay clientes", "Registra primero un cliente para poder asociarle un equipo.");
            return;
        }

        boolean editing = existingDevice != null;
        Device device = editing ? existingDevice : new Device();
        ObservableList<Customer> customerChoices = FXCollections.observableArrayList(customers);
        if (editing && customers.stream().noneMatch(customer -> customer.getId().equals(device.getCustomer().getId()))) {
            customerChoices.add(device.getCustomer());
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Editar equipo" : "Nuevo equipo");
        dialog.setHeaderText("Registra la identificación y condición de recepción del equipo.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        ComboBox<Customer> customerField = new ComboBox<>(customerChoices);
        customerField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customerDisplayName(customer);
            }

            @Override
            public Customer fromString(String value) {
                return customerChoices.stream().filter(customer -> customerDisplayName(customer).equals(value))
                        .findFirst().orElse(null);
            }
        });
        if (editing) {
            customerField.setValue(customerChoices.stream()
                    .filter(customer -> customer.getId().equals(device.getCustomer().getId()))
                    .findFirst().orElse(null));
        }

        ComboBox<DeviceType> typeField = new ComboBox<>(FXCollections.observableArrayList(DeviceType.values()));
        typeField.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(DeviceType type) {
                return type == null ? "" : deviceTypeLabel(type);
            }

            @Override
            public DeviceType fromString(String value) {
                return java.util.Arrays.stream(DeviceType.values())
                        .filter(type -> deviceTypeLabel(type).equals(value)).findFirst().orElse(null);
            }
        });
        typeField.setValue(device.getDeviceType() == null ? DeviceType.CELULAR : device.getDeviceType());

        TextField brandField = new TextField(valueOrEmpty(device.getBrand()));
        TextField modelField = new TextField(valueOrEmpty(device.getModel()));
        TextField serialField = new TextField(valueOrEmpty(device.getSerialNumber()));
        TextField imeiField = new TextField(valueOrEmpty(device.getImei()));
        TextField colorField = new TextField(valueOrEmpty(device.getColor()));
        PasswordField pinField = new PasswordField();
        pinField.setText(valueOrEmpty(device.getPasswordOrPin()));
        TextArea accessoriesField = new TextArea(valueOrEmpty(device.getAccessories()));
        TextArea conditionField = new TextArea(valueOrEmpty(device.getPhysicalCondition()));
        TextArea notesField = new TextArea(valueOrEmpty(device.getNotes()));
        accessoriesField.setPrefRowCount(2);
        conditionField.setPrefRowCount(2);
        notesField.setPrefRowCount(2);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(9);
        form.setPadding(new Insets(10, 4, 4, 4));
        addDeviceFormField(form, "Cliente", customerField, 0);
        addDeviceFormField(form, "Tipo", typeField, 1);
        addDeviceFormField(form, "Marca", brandField, 2);
        addDeviceFormField(form, "Modelo", modelField, 3);
        addDeviceFormField(form, "N° de serie", serialField, 4);
        addDeviceFormField(form, "IMEI", imeiField, 5);
        addDeviceFormField(form, "Color", colorField, 6);
        addDeviceFormField(form, "PIN / contraseña", pinField, 7);
        addDeviceFormField(form, "Accesorios", accessoriesField, 8);
        addDeviceFormField(form, "Estado físico", conditionField, 9);
        addDeviceFormField(form, "Notas", notesField, 10);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(600);

        while (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            device.setCustomer(customerField.getValue());
            device.setDeviceType(typeField.getValue());
            device.setBrand(brandField.getText());
            device.setModel(modelField.getText());
            device.setSerialNumber(serialField.getText());
            device.setImei(imeiField.getText());
            device.setColor(colorField.getText());
            device.setPasswordOrPin(pinField.getText());
            device.setAccessories(accessoriesField.getText());
            device.setPhysicalCondition(conditionField.getText());
            device.setNotes(notesField.getText());
            try {
                deviceDataService.save(device);
                renderModule("equipos");
                return;
            } catch (IllegalArgumentException exception) {
                showMessage(Alert.AlertType.ERROR, "No se pudo guardar el equipo", exception.getMessage());
            }
        }
    }

    private void addDeviceFormField(GridPane form, String label, Node control, int row) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("metric-label");
        form.add(fieldLabel, 0, row);
        form.add(control, 1, row);
        GridPane.setHgrow(control, Priority.ALWAYS);
        if (control instanceof TextField || control instanceof ComboBox<?>) {
            control.setStyle("-fx-pref-width: 360px;");
        }
    }

    private String deviceTypeLabel(DeviceType type) {
        return switch (type) {
            case CELULAR -> "Celular";
            case LAPTOP -> "Portátil";
            case PC -> "PC";
            case IMPRESORA -> "Impresora";
            case TV -> "Televisor";
            case CONSOLA -> "Consola";
            case MONITOR -> "Monitor";
            case TABLET -> "Tablet";
            case OTRO -> "Otro";
        };
    }

    private void showCustomerDialog(Customer existingCustomer) {
        boolean editing = existingCustomer != null;
        Customer customer = editing ? existingCustomer : new Customer();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Editar cliente" : "Nuevo cliente");
        dialog.setHeaderText(editing ? "Actualiza los datos del cliente." : "Registra una persona o empresa.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        ComboBox<CustomerType> customerType = new ComboBox<>(FXCollections.observableArrayList(CustomerType.values()));
        customerType.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CustomerType value) {
                return value == CustomerType.COMPANY ? "Empresa" : "Persona";
            }

            @Override
            public CustomerType fromString(String value) {
                return "Empresa".equals(value) ? CustomerType.COMPANY : CustomerType.PERSON;
            }
        });
        customerType.setValue(customer.getCustomerType() == null ? CustomerType.PERSON : customer.getCustomerType());

        TextField firstName = new TextField(valueOrEmpty(customer.getFirstName()));
        TextField lastName = new TextField(valueOrEmpty(customer.getLastName()));
        TextField businessName = new TextField(valueOrEmpty(customer.getBusinessName()));
        TextField document = new TextField(valueOrEmpty(customer.getDocument()));
        TextField phone = new TextField(valueOrEmpty(customer.getPhone()));
        TextField email = new TextField(valueOrEmpty(customer.getEmail()));
        TextField city = new TextField(valueOrEmpty(customer.getCity()));
        TextField province = new TextField(valueOrEmpty(customer.getProvince()));
        TextField address = new TextField(valueOrEmpty(customer.getAddress()));
        TextArea notes = new TextArea(valueOrEmpty(customer.getNotes()));
        notes.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 4, 4, 4));
        form.add(new Label("Tipo"), 0, 0);
        form.add(customerType, 1, 0);
        Label firstNameLabel = new Label("Nombre");
        Label lastNameLabel = new Label("Apellido");
        Label businessNameLabel = new Label("Empresa");
        form.add(firstNameLabel, 0, 1);
        form.add(firstName, 1, 1);
        form.add(lastNameLabel, 0, 2);
        form.add(lastName, 1, 2);
        form.add(businessNameLabel, 0, 3);
        form.add(businessName, 1, 3);
        form.add(new Label("Documento"), 0, 4);
        form.add(document, 1, 4);
        form.add(new Label("Teléfono"), 0, 5);
        form.add(phone, 1, 5);
        form.add(new Label("Correo"), 0, 6);
        form.add(email, 1, 6);
        form.add(new Label("Ciudad"), 0, 7);
        form.add(city, 1, 7);
        form.add(new Label("Provincia"), 0, 8);
        form.add(province, 1, 8);
        form.add(new Label("Dirección"), 0, 9);
        form.add(address, 1, 9);
        form.add(new Label("Notas"), 0, 10);
        form.add(notes, 1, 10);
        boolean initialCompany = customerType.getValue() == CustomerType.COMPANY;
        setManagedVisible(firstNameLabel, !initialCompany);
        setManagedVisible(firstName, !initialCompany);
        setManagedVisible(lastNameLabel, !initialCompany);
        setManagedVisible(lastName, !initialCompany);
        setManagedVisible(businessNameLabel, initialCompany);
        setManagedVisible(businessName, initialCompany);
        customerType.valueProperty().addListener((observable, previous, selected) -> {
            boolean isCompany = selected == CustomerType.COMPANY;
            setManagedVisible(firstNameLabel, !isCompany);
            setManagedVisible(firstName, !isCompany);
            setManagedVisible(lastNameLabel, !isCompany);
            setManagedVisible(lastName, !isCompany);
            setManagedVisible(businessNameLabel, isCompany);
            setManagedVisible(businessName, isCompany);
        });

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(520);
        while (dialog.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            customer.setCustomerType(customerType.getValue());
            customer.setFirstName(firstName.getText());
            customer.setLastName(lastName.getText());
            customer.setBusinessName(businessName.getText());
            customer.setDocument(document.getText());
            customer.setPhone(phone.getText());
            customer.setEmail(email.getText());
            customer.setCity(city.getText());
            customer.setProvince(province.getText());
            customer.setAddress(address.getText());
            customer.setNotes(notes.getText());
            try {
                customerDataService.save(customer);
                renderModule("clientes");
                return;
            } catch (IllegalArgumentException exception) {
                showMessage(Alert.AlertType.ERROR, "No se pudo guardar el cliente", exception.getMessage());
            }
        }
    }

    private void addCustomerDetail(GridPane grid, int row, String label, Label value) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("metric-label");
        value.setWrapText(true);
        grid.add(fieldLabel, 0, row);
        grid.add(value, 1, row);
    }

    private void setManagedVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String displayLocation(Customer customer) {
        String city = customer.getCity() == null ? "" : customer.getCity();
        String province = customer.getProvince() == null ? "" : customer.getProvince();
        String location = String.join(", ", List.of(city, province).stream().filter(value -> !value.isBlank()).toList());
        return location.isBlank() ? "-" : location;
    }

    private String displayValue(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String customerDisplayName(Customer customer) {
        if (customer.getCustomerType() == CustomerType.COMPANY) {
            return displayValue(customer.getBusinessName());
        }
        return String.join(" ", List.of(valueOrEmpty(customer.getFirstName()), valueOrEmpty(customer.getLastName())))
                .trim();
    }

    private Node createInventoryView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        List<Product> products = productDataService.getProducts();

        VBox summary = new VBox(12);
        summary.getStyleClass().add("info-panel");

        Label title = new Label("Inventario");
        title.getStyleClass().add("module-title");

        Label subtitle = new Label("Control de stock, proveedores y movimientos del taller.");
        subtitle.getStyleClass().add("module-description");

        ObservableList<InventoryRow> inventoryRows = createInventoryRows(products);
        FilterToolbar<InventoryRow> filters = createFilterToolbar(
            "Buscar producto o SKU", "Todos", inventoryRows,
            row -> row.skuProperty().get() + " " + row.nameProperty().get(),
            row -> row.statusProperty().get(),
            "Todos", "Stock bajo", "OK"
        );

        HBox detailGrid = new HBox(18);
        int totalUnits = products.stream().mapToInt(Product::getQuantityOnHand).sum();
        int lowStockCount = (int) products.stream().filter(Product::isLowStock).count();
        detailGrid.getChildren().addAll(
            createDetailField("Stock total", String.valueOf(totalUnits)),
            createDetailField("Bajo mínimo", String.valueOf(lowStockCount)),
            createDetailField("Disponibles", String.valueOf(products.size() - lowStockCount)),
            createDetailField("Estado", "Operativo")
        );

        summary.getChildren().addAll(title, subtitle, filters.node(), detailGrid);

        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(
                createMetricCard("Productos", String.valueOf(products.size())),
                createMetricCard("Stock bajo", String.valueOf(lowStockCount)),
                createMetricCard("Entradas", String.valueOf(products.stream().mapToInt(Product::getQuantityOnHand).sum())),
                createMetricCard("Salidas", String.valueOf(Math.max(0, products.size() - lowStockCount)))
        );

        TableView<InventoryRow> table = new TableView<>();
        table.setPlaceholder(new Label("No hay productos registrados."));
        table.setItems(filters.filteredRows());

        TableColumn<InventoryRow, String> skuColumn = new TableColumn<>("SKU");
        skuColumn.setCellValueFactory(cell -> cell.getValue().skuProperty());

        TableColumn<InventoryRow, String> nameColumn = new TableColumn<>("Producto");
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<InventoryRow, String> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cell -> cell.getValue().stockProperty());

        TableColumn<InventoryRow, String> priceColumn = new TableColumn<>("Precio");
        priceColumn.setCellValueFactory(cell -> cell.getValue().priceProperty());

        TableColumn<InventoryRow, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        table.getColumns().addAll(skuColumn, nameColumn, stockColumn, priceColumn, statusColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().addAll(summary, metrics, table);
        return root;
    }

    private Node createSalesView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        List<Sale> sales = saleDataService.getRecentSales(3);
        BigDecimal totalSales = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = sales.stream().map(Sale::getPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPending = totalSales.subtract(totalPaid).max(BigDecimal.ZERO);

        VBox summary = new VBox(12);
        summary.getStyleClass().add("info-panel");

        Label title = new Label("Ventas");
        title.getStyleClass().add("module-title");

        Label subtitle = new Label("Punto de venta con productos, cobranzas y comprobantes del día.");
        subtitle.getStyleClass().add("module-description");

        ObservableList<SalesRow> salesRows = createSalesRows(sales);
        FilterToolbar<SalesRow> filters = createFilterToolbar(
            "Buscar venta o producto", "Todos", salesRows,
            row -> row.saleNumberProperty().get() + " " + row.productProperty().get(),
            row -> row.statusProperty().get(),
            "Todos", "Pagada", "Pendiente", "Cancelada"
        );

        HBox detailGrid = new HBox(18);
        detailGrid.getChildren().addAll(
            createDetailField("Total ventas", formatCurrency(totalSales)),
            createDetailField("Cobrado", formatCurrency(totalPaid)),
            createDetailField("Pendiente", formatCurrency(totalPending)),
            createDetailField("Ticket medio", formatCurrency(sales.isEmpty() ? BigDecimal.ZERO : totalSales.divide(BigDecimal.valueOf(sales.size()), 2, java.math.RoundingMode.HALF_UP)))
        );

        summary.getChildren().addAll(title, subtitle, filters.node(), detailGrid);

        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(
                createMetricCard("Nuevas", String.valueOf(sales.size())),
                createMetricCard("Cobrado", formatCurrency(totalPaid)),
                createMetricCard("Pendiente", formatCurrency(totalPending)),
                createMetricCard("Ticket medio", formatCurrency(sales.isEmpty() ? BigDecimal.ZERO : totalSales.divide(BigDecimal.valueOf(sales.size()), 2, java.math.RoundingMode.HALF_UP)))
        );

        TableView<SalesRow> itemsTable = new TableView<>();
        itemsTable.setPlaceholder(new Label("No hay líneas de venta."));
        itemsTable.setItems(filters.filteredRows());

        TableColumn<SalesRow, String> saleColumn = new TableColumn<>("Venta");
        saleColumn.setCellValueFactory(cell -> cell.getValue().saleNumberProperty());

        TableColumn<SalesRow, String> productColumn = new TableColumn<>("Producto");
        productColumn.setCellValueFactory(cell -> cell.getValue().productProperty());

        TableColumn<SalesRow, String> saleStatusColumn = new TableColumn<>("Estado");
        saleStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        TableColumn<SalesRow, String> qtyColumn = new TableColumn<>("Cantidad");
        qtyColumn.setCellValueFactory(cell -> cell.getValue().quantityProperty());

        TableColumn<SalesRow, String> unitColumn = new TableColumn<>("P. unitario");
        unitColumn.setCellValueFactory(cell -> cell.getValue().unitPriceProperty());

        TableColumn<SalesRow, String> subtotalColumn = new TableColumn<>("Subtotal");
        subtotalColumn.setCellValueFactory(cell -> cell.getValue().subtotalProperty());

        itemsTable.getColumns().addAll(saleColumn, productColumn, saleStatusColumn, qtyColumn, unitColumn, subtotalColumn);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox payments = new VBox(12);
        payments.getStyleClass().add("info-panel");

        Label paymentTitle = new Label("Cobros asociados");
        paymentTitle.getStyleClass().add("module-summary");

        TableView<PaymentRow> paymentTable = new TableView<>();
        paymentTable.setPlaceholder(new Label("No hay cobros registrados."));
        paymentTable.setItems(createPaymentRowsFromSales(sales));

        TableColumn<PaymentRow, String> methodColumn = new TableColumn<>("Método");
        methodColumn.setCellValueFactory(cell -> cell.getValue().methodProperty());

        TableColumn<PaymentRow, String> referenceColumn = new TableColumn<>("Referencia");
        referenceColumn.setCellValueFactory(cell -> cell.getValue().referenceProperty());

        TableColumn<PaymentRow, String> paymentAmountColumn = new TableColumn<>("Monto");
        paymentAmountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty());

        paymentTable.getColumns().addAll(methodColumn, referenceColumn, paymentAmountColumn);
        paymentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        payments.getChildren().addAll(paymentTitle, paymentTable);

        root.getChildren().addAll(summary, metrics, itemsTable, payments);
        return root;
    }

    private Node createBudgetView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        List<Budget> budgets = budgetDataService.getRecentBudgets(4);
        Budget selectedBudget = budgets.isEmpty() ? null : budgets.get(0);
        BigDecimal totalCollected = budgets.stream()
            .map(Budget::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAdvance = budgets.stream()
            .flatMap(budget -> budgetDataService.getPaymentsForBudget(budget).stream())
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBalance = budgets.stream()
            .map(Budget::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .subtract(totalAdvance);

        VBox summary = new VBox(12);
        summary.getStyleClass().add("info-panel");

        Label title = new Label("Presupuestos");
        title.getStyleClass().add("module-title");

        Label subtitle = new Label("Cotizaciones aprobadas, anticipos y saldos pendientes por cliente.");
        subtitle.getStyleClass().add("module-description");

        ObservableList<BudgetRow> budgetRows = createBudgetRows(budgets);
        FilterToolbar<BudgetRow> filters = createFilterToolbar(
            "Buscar presupuesto o cliente", "Todos", budgetRows,
            row -> row.numberProperty().get() + " " + row.customerProperty().get(),
            row -> row.statusProperty().get(),
            "Todos", "Aprobado", "Pendiente", "Otros"
        );

        HBox detailGrid = new HBox(18);
        detailGrid.getChildren().addAll(
            createDetailField("Cliente principal", budgets.isEmpty() ? "Sin datos" : resolveCustomerName(budgets.get(0))),
            createDetailField("Anticipos", formatCurrency(totalAdvance)),
            createDetailField("Saldo", formatCurrency(totalBalance)),
            createDetailField("Estado", budgets.isEmpty() ? "Sin registros" : "Activo")
        );

        summary.getChildren().addAll(title, subtitle, filters.node(), detailGrid);

        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(
                createMetricCard("Pendientes", String.valueOf(budgets.size())),
                createMetricCard("Aprobados", String.valueOf((int) budgets.stream().filter(b -> b.getStatus() != null && b.getStatus().name().equals("APPROVED")).count())),
                createMetricCard("Anticipos", formatCurrency(totalAdvance)),
                createMetricCard("Saldo", formatCurrency(totalBalance))
        );

        TableView<BudgetRow> budgetsTable = new TableView<>();
        budgetsTable.setPlaceholder(new Label("No hay presupuestos registrados."));
        budgetsTable.setItems(filters.filteredRows());

        TableColumn<BudgetRow, String> numberColumn = new TableColumn<>("N° presupuesto");
        numberColumn.setCellValueFactory(cell -> cell.getValue().numberProperty());

        TableColumn<BudgetRow, String> customerColumn = new TableColumn<>("Cliente");
        customerColumn.setCellValueFactory(cell -> cell.getValue().customerProperty());

        TableColumn<BudgetRow, String> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(cell -> cell.getValue().totalProperty());

        TableColumn<BudgetRow, String> advanceColumn = new TableColumn<>("Anticipo");
        advanceColumn.setCellValueFactory(cell -> cell.getValue().advanceProperty());

        TableColumn<BudgetRow, String> balanceColumn = new TableColumn<>("Saldo");
        balanceColumn.setCellValueFactory(cell -> cell.getValue().balanceProperty());

        TableColumn<BudgetRow, String> budgetStatusColumn = new TableColumn<>("Estado");
        budgetStatusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        budgetsTable.getColumns().addAll(numberColumn, customerColumn, totalColumn, advanceColumn, balanceColumn, budgetStatusColumn);
        budgetsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox detail = new VBox(12);
        detail.getStyleClass().add("info-panel");

        Label detailTitle = new Label("Detalle del presupuesto");
        detailTitle.getStyleClass().add("module-summary");

        HBox detailHeader = new HBox(18);
        detailHeader.setSpacing(18);

        VBox clientBox = new VBox(6);
        Label clientLabel = new Label("Cliente");
        clientLabel.getStyleClass().add("metric-label");
        Label clientValue = new Label(selectedBudget == null ? "Sin datos" : resolveCustomerName(selectedBudget));
        clientValue.setStyle("-fx-font-weight: 700; -fx-font-size: 16px;");
        clientBox.getChildren().addAll(clientLabel, clientValue);

        VBox orderBox = new VBox(6);
        Label orderLabel = new Label("Orden");
        orderLabel.getStyleClass().add("metric-label");
        Label orderValue = new Label(selectedBudget == null ? "-" : resolveOrderNumber(selectedBudget));
        orderValue.setStyle("-fx-font-weight: 700; -fx-font-size: 16px;");
        orderBox.getChildren().addAll(orderLabel, orderValue);

        VBox statusBox = new VBox(6);
        Label statusLabel = new Label("Estado");
        statusLabel.getStyleClass().add("metric-label");
        Label statusValue = new Label(selectedBudget == null ? "Sin datos" : selectedBudget.getStatus().name());
        statusValue.setStyle("-fx-font-weight: 700; -fx-text-fill: #15803d; -fx-font-size: 16px;");
        statusBox.getChildren().addAll(statusLabel, statusValue);

        detailHeader.getChildren().addAll(clientBox, orderBox, statusBox);

        HBox repairBox = new HBox(24);
        repairBox.setSpacing(24);
        VBox repairSummary = new VBox(8);
        Label repairTitle = new Label("Reparación indicada");
        repairTitle.getStyleClass().add("metric-label");
        Label repairText = new Label(selectedBudget == null || selectedBudget.getServiceOrder() == null || selectedBudget.getServiceOrder().getDeclaredFailure() == null ? "Sin diagnóstico registrado." : selectedBudget.getServiceOrder().getDeclaredFailure());
        repairText.setWrapText(true);
        repairText.setMaxWidth(520);
        repairSummary.getChildren().addAll(repairTitle, repairText);

        VBox paymentSummary = new VBox(8);
        Label paymentTitle = new Label("Pago");
        paymentTitle.getStyleClass().add("metric-label");
        BigDecimal selectedAdvance = selectedBudget == null ? BigDecimal.ZERO : getPaymentTotal(selectedBudget);
        BigDecimal selectedBalance = selectedBudget == null ? BigDecimal.ZERO : selectedBudget.getTotal().subtract(selectedAdvance);
        Label paymentText = new Label("Método: " + (selectedBudget == null ? "-" : "Tarjeta") + " · Total: " + (selectedBudget == null ? "-" : formatCurrency(selectedBudget.getTotal())) + " · Anticipo: " + formatCurrency(selectedAdvance) + " · Saldo: " + formatCurrency(selectedBalance));
        paymentText.setWrapText(true);
        paymentText.setMaxWidth(420);
        paymentSummary.getChildren().addAll(paymentTitle, paymentText);

        repairBox.getChildren().addAll(repairSummary, paymentSummary);
        detail.getChildren().addAll(detailTitle, detailHeader, repairBox);

        VBox payments = new VBox(12);
        payments.getStyleClass().add("info-panel");

        Label paymentTableTitle = new Label("Pagos y cobros");
        paymentTableTitle.getStyleClass().add("module-summary");

        TableView<PaymentRow> paymentTable = new TableView<>();
        paymentTable.setPlaceholder(new Label("No hay pagos registrados."));
        paymentTable.setItems(createBudgetPaymentRows(selectedBudget));

        TableColumn<PaymentRow, String> methodColumn = new TableColumn<>("Método");
        methodColumn.setCellValueFactory(cell -> cell.getValue().methodProperty());

        TableColumn<PaymentRow, String> referenceColumn = new TableColumn<>("Referencia");
        referenceColumn.setCellValueFactory(cell -> cell.getValue().referenceProperty());

        TableColumn<PaymentRow, String> amountColumn = new TableColumn<>("Monto");
        amountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty());

        paymentTable.getColumns().addAll(methodColumn, referenceColumn, amountColumn);
        paymentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        payments.getChildren().addAll(paymentTableTitle, paymentTable);

        root.getChildren().addAll(summary, metrics, budgetsTable, detail, payments);
        return root;
    }

    private Node createCashView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));

        CashSession session = cashDataService.getCurrentSession();

        VBox summary = new VBox(12);
        summary.getStyleClass().add("info-panel");

        Label title = new Label("Caja");
        title.getStyleClass().add("module-title");

        Label subtitle = new Label("Apertura, cierre, movimientos y arqueo del turno.");
        subtitle.getStyleClass().add("module-description");

        ObservableList<CashRow> cashRows = createCashRows(session);
        FilterToolbar<CashRow> filters = createFilterToolbar(
            "Buscar descripción o tipo", "Todos", cashRows,
            row -> row.timeProperty().get() + " " + row.typeProperty().get() + " "
                + row.descriptionProperty().get() + " " + row.amountProperty().get(),
            this::getCashMovementCategory,
            "Todos", "Entradas", "Salidas", "Otros"
        );

        HBox detailGrid = new HBox(18);
        detailGrid.getChildren().addAll(
            createDetailField("Apertura", formatCurrency(session.getOpeningAmount())),
            createDetailField("Ingresos", formatCurrency(session.getTotalIn())),
            createDetailField("Egresos", formatCurrency(session.getTotalOut())),
            createDetailField("Saldo actual", formatCurrency(session.getCurrentBalance()))
        );

        summary.getChildren().addAll(title, subtitle, filters.node(), detailGrid);

        HBox metrics = new HBox(18);
        metrics.getChildren().addAll(
                createMetricCard("Apertura", formatCurrency(session.getOpeningAmount())),
                createMetricCard("Ingresos", formatCurrency(session.getTotalIn())),
                createMetricCard("Egresos", formatCurrency(session.getTotalOut())),
                createMetricCard("Saldo", formatCurrency(session.getCurrentBalance()))
        );

        TableView<CashRow> table = new TableView<>();
        table.setPlaceholder(new Label("No hay movimientos de caja."));
        table.setItems(filters.filteredRows());

        TableColumn<CashRow, String> timeColumn = new TableColumn<>("Hora");
        timeColumn.setCellValueFactory(cell -> cell.getValue().timeProperty());

        TableColumn<CashRow, String> typeColumn = new TableColumn<>("Tipo");
        typeColumn.setCellValueFactory(cell -> cell.getValue().typeProperty());

        TableColumn<CashRow, String> descriptionColumn = new TableColumn<>("Descripción");
        descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());

        TableColumn<CashRow, String> amountColumn = new TableColumn<>("Monto");
        amountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty());

        table.getColumns().addAll(timeColumn, typeColumn, descriptionColumn, amountColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().addAll(summary, metrics, table);
        return root;
    }

    private ObservableList<InventoryRow> createInventoryRows(List<Product> products) {
        ObservableList<InventoryRow> rows = FXCollections.observableArrayList();
        for (Product product : products) {
            rows.add(new InventoryRow(
                    product.getSku(),
                    product.getName(),
                    String.valueOf(product.getQuantityOnHand()),
                    formatCurrency(product.getSalePrice()),
                    product.isLowStock() ? "Stock bajo" : "OK"
            ));
        }
        return rows;
    }

    private ObservableList<SalesRow> createSalesRows(List<Sale> sales) {
        ObservableList<SalesRow> rows = FXCollections.observableArrayList();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                rows.add(new SalesRow(
                        item.getProduct() == null ? "Producto" : item.getProduct().getName(),
                        String.valueOf(item.getQuantity()),
                        formatCurrency(item.getUnitPrice()),
                            formatCurrency(item.getSubtotal()),
                            sale.getSaleNumber(),
                            getSalePaymentStatus(sale)
                ));
            }
        }
        return rows;
    }

    private ObservableList<PaymentRow> createPaymentRowsFromSales(List<Sale> sales) {
        ObservableList<PaymentRow> rows = FXCollections.observableArrayList();
        for (Sale sale : sales) {
            rows.add(new PaymentRow(
                    sale.getStatus().name(),
                    sale.getSaleNumber(),
                    formatCurrency(sale.getPaid())
            ));
        }
        return rows;
    }

    private ObservableList<PaymentRow> createPaymentRows() {
        return FXCollections.observableArrayList(
                new PaymentRow("Tarjeta", "TRX-2048", "€240.00"),
                new PaymentRow("Efectivo", "EF-112", "€120.00"),
                new PaymentRow("Transferencia", "IBAN-889", "€60.00")
        );
    }

    private ObservableList<BudgetRow> createBudgetRows(List<Budget> budgets) {
        ObservableList<BudgetRow> rows = FXCollections.observableArrayList();
        for (Budget budget : budgets) {
            BigDecimal paymentTotal = getPaymentTotal(budget);
            BigDecimal balance = budget.getTotal().subtract(paymentTotal);
            rows.add(new BudgetRow(
                    budget.getBudgetNumber(),
                    resolveCustomerName(budget),
                    formatCurrency(budget.getTotal()),
                    formatCurrency(paymentTotal),
                        formatCurrency(balance),
                        getBudgetStatusLabel(budget.getStatus())
            ));
        }
        return rows;
    }

    private ObservableList<PaymentRow> createBudgetPaymentRows(Budget budget) {
        ObservableList<PaymentRow> rows = FXCollections.observableArrayList();
        if (budget == null) {
            return rows;
        }

        for (Payment payment : budgetDataService.getPaymentsForBudget(budget)) {
            rows.add(new PaymentRow(
                    payment.getPaymentMethod() == null ? "-" : payment.getPaymentMethod().name(),
                    payment.getReferenceNumber() == null ? "-" : payment.getReferenceNumber(),
                    formatCurrency(payment.getAmount())
            ));
        }
        return rows;
    }

    private String resolveCustomerName(Budget budget) {
        if (budget == null || budget.getServiceOrder() == null || budget.getServiceOrder().getCustomer() == null) {
            return "Sin cliente";
        }

        Customer customer = budget.getServiceOrder().getCustomer();
        if (customer.getFirstName() != null || customer.getLastName() != null) {
            return String.join(" ",
                    customer.getFirstName() == null ? "" : customer.getFirstName(),
                    customer.getLastName() == null ? "" : customer.getLastName()
            ).trim();
        }
        return customer.getBusinessName() != null ? customer.getBusinessName() : "Sin cliente";
    }

    private String resolveOrderNumber(Budget budget) {
        if (budget == null || budget.getServiceOrder() == null) {
            return "-";
        }
        return budget.getServiceOrder().getOrderNumber();
    }

    private BigDecimal getPaymentTotal(Budget budget) {
        if (budget == null) {
            return BigDecimal.ZERO;
        }
        return budgetDataService.getPaymentsForBudget(budget).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "€0.00";
        }
        return "€" + String.format("%.2f", amount);
    }

    private ObservableList<CashRow> createCashRows(CashSession session) {
        ObservableList<CashRow> rows = FXCollections.observableArrayList();
        if (session == null) {
            return rows;
        }

        for (CashMovement movement : session.getMovements()) {
            rows.add(new CashRow(
                    movement.getMovementAt().toLocalTime().toString(),
                    movement.getMovementType().name(),
                    movement.getDescription(),
                    formatCurrency(movement.getAmount())
            ));
        }
        return rows;
    }

    private ObservableList<DashboardRow> createDashboardRows() {
        ObservableList<DashboardRow> rows = FXCollections.observableArrayList();
        for (Sale sale : saleDataService.getRecentSales(5)) {
            rows.add(new DashboardRow(sale.getSaleNumber(), formatCurrency(sale.getTotal()), sale.getStatus().name()));
        }
        return rows;
    }

    private Button createActionButton(String label, String moduleKey) {
        Button actionButton = new Button(label);
        actionButton.getStyleClass().add("quick-action-button");
        actionButton.setOnAction(event -> renderModule(moduleKey));
        return actionButton;
    }

    private <T> FilterToolbar<T> createFilterToolbar(
            String promptText,
            String defaultFilter,
            ObservableList<T> sourceRows,
            Function<T, String> searchableText,
            Function<T, String> statusText,
            String... options
    ) {
        HBox filterBar = new HBox(12);
        filterBar.getStyleClass().add("filter-toolbar");

        TextField searchField = new TextField();
        searchField.setPromptText(promptText);
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll(options.length == 0 ? List.of(defaultFilter) : List.of(options));
        statusFilter.setValue(defaultFilter);
        statusFilter.setPrefWidth(160);

        Button filterButton = new Button("Aplicar");
        filterButton.getStyleClass().add("primary-button");

        Button clearButton = new Button("Limpiar");
        clearButton.getStyleClass().add("secondary-button");

        FilteredList<T> filteredRows = new FilteredList<>(sourceRows);
        Runnable updateFilter = () -> {
            String query = normalizeText(searchField.getText());
            String selectedStatus = statusFilter.getValue();
            filteredRows.setPredicate(row -> {
                boolean matchesText = query.isBlank()
                        || normalizeText(searchableText.apply(row)).contains(query);
                boolean matchesStatus = selectedStatus == null
                        || selectedStatus.equals(defaultFilter)
                        || selectedStatus.equalsIgnoreCase(statusText.apply(row));
                return matchesText && matchesStatus;
            });
        };

        searchField.textProperty().addListener((observable, previous, current) -> updateFilter.run());
        statusFilter.valueProperty().addListener((observable, previous, current) -> updateFilter.run());
        filterButton.setOnAction(event -> updateFilter.run());
        clearButton.setOnAction(event -> {
            searchField.clear();
            statusFilter.setValue(defaultFilter);
            updateFilter.run();
        });

        filterBar.getChildren().addAll(searchField, statusFilter, filterButton, clearButton);
        return new FilterToolbar<>(filterBar, filteredRows);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private String getSalePaymentStatus(Sale sale) {
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            return "Cancelada";
        }
        return sale.getBalance().compareTo(BigDecimal.ZERO) <= 0 ? "Pagada" : "Pendiente";
    }

    private String getBudgetStatusLabel(BudgetStatus status) {
        if (status == BudgetStatus.APPROVED) {
            return "Aprobado";
        }
        if (status == BudgetStatus.PENDING) {
            return "Pendiente";
        }
        return "Otros";
    }

    private String getCashMovementCategory(CashRow row) {
        return switch (row.typeProperty().get()) {
            case "OPENING", "SALE" -> "Entradas";
            case "REFUND", "EXPENSE" -> "Salidas";
            default -> "Otros";
        };
    }

    private VBox createDetailField(String label, String value) {
        VBox field = new VBox(8);
        field.getStyleClass().add("detail-field");

        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("metric-label");

        Label fieldValue = new Label(value);
        fieldValue.getStyleClass().add("detail-value");

        field.getChildren().addAll(fieldLabel, fieldValue);
        return field;
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

    private static class InventoryRow {
        private final javafx.beans.property.SimpleStringProperty sku;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty stock;
        private final javafx.beans.property.SimpleStringProperty price;
        private final javafx.beans.property.SimpleStringProperty status;

        private InventoryRow(String sku, String name, String stock, String price, String status) {
            this.sku = new javafx.beans.property.SimpleStringProperty(sku);
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.stock = new javafx.beans.property.SimpleStringProperty(stock);
            this.price = new javafx.beans.property.SimpleStringProperty(price);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty skuProperty() {
            return sku;
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.StringProperty stockProperty() {
            return stock;
        }

        public javafx.beans.property.StringProperty priceProperty() {
            return price;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    private static class SalesRow {
        private final javafx.beans.property.SimpleStringProperty saleNumber;
        private final javafx.beans.property.SimpleStringProperty product;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty quantity;
        private final javafx.beans.property.SimpleStringProperty unitPrice;
        private final javafx.beans.property.SimpleStringProperty subtotal;

        private SalesRow(String product, String quantity, String unitPrice, String subtotal, String saleNumber, String status) {
            this.saleNumber = new javafx.beans.property.SimpleStringProperty(saleNumber);
            this.product = new javafx.beans.property.SimpleStringProperty(product);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.quantity = new javafx.beans.property.SimpleStringProperty(quantity);
            this.unitPrice = new javafx.beans.property.SimpleStringProperty(unitPrice);
            this.subtotal = new javafx.beans.property.SimpleStringProperty(subtotal);
        }

        public javafx.beans.property.StringProperty saleNumberProperty() {
            return saleNumber;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.StringProperty productProperty() {
            return product;
        }

        public javafx.beans.property.StringProperty quantityProperty() {
            return quantity;
        }

        public javafx.beans.property.StringProperty unitPriceProperty() {
            return unitPrice;
        }

        public javafx.beans.property.StringProperty subtotalProperty() {
            return subtotal;
        }
    }

    private static class BudgetRow {
        private final javafx.beans.property.SimpleStringProperty number;
        private final javafx.beans.property.SimpleStringProperty customer;
        private final javafx.beans.property.SimpleStringProperty total;
        private final javafx.beans.property.SimpleStringProperty advance;
        private final javafx.beans.property.SimpleStringProperty balance;
        private final javafx.beans.property.SimpleStringProperty status;

        private BudgetRow(String number, String customer, String total, String advance, String balance, String status) {
            this.number = new javafx.beans.property.SimpleStringProperty(number);
            this.customer = new javafx.beans.property.SimpleStringProperty(customer);
            this.total = new javafx.beans.property.SimpleStringProperty(total);
            this.advance = new javafx.beans.property.SimpleStringProperty(advance);
            this.balance = new javafx.beans.property.SimpleStringProperty(balance);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty numberProperty() {
            return number;
        }

        public javafx.beans.property.StringProperty customerProperty() {
            return customer;
        }

        public javafx.beans.property.StringProperty totalProperty() {
            return total;
        }

        public javafx.beans.property.StringProperty advanceProperty() {
            return advance;
        }

        public javafx.beans.property.StringProperty balanceProperty() {
            return balance;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    private static class PaymentRow {
        private final javafx.beans.property.SimpleStringProperty method;
        private final javafx.beans.property.SimpleStringProperty reference;
        private final javafx.beans.property.SimpleStringProperty amount;

        private PaymentRow(String method, String reference, String amount) {
            this.method = new javafx.beans.property.SimpleStringProperty(method);
            this.reference = new javafx.beans.property.SimpleStringProperty(reference);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
        }

        public javafx.beans.property.StringProperty methodProperty() {
            return method;
        }

        public javafx.beans.property.StringProperty referenceProperty() {
            return reference;
        }

        public javafx.beans.property.StringProperty amountProperty() {
            return amount;
        }
    }

    private static class CashRow {
        private final javafx.beans.property.SimpleStringProperty time;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty amount;

        private CashRow(String time, String type, String description, String amount) {
            this.time = new javafx.beans.property.SimpleStringProperty(time);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.description = new javafx.beans.property.SimpleStringProperty(description);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
        }

        public javafx.beans.property.StringProperty timeProperty() {
            return time;
        }

        public javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        public javafx.beans.property.StringProperty descriptionProperty() {
            return description;
        }

        public javafx.beans.property.StringProperty amountProperty() {
            return amount;
        }
    }

    private static class FilterToolbar<T> {
        private final HBox node;
        private final FilteredList<T> filteredRows;

        private FilterToolbar(HBox node, FilteredList<T> filteredRows) {
            this.node = node;
            this.filteredRows = filteredRows;
        }

        private HBox node() {
            return node;
        }

        private FilteredList<T> filteredRows() {
            return filteredRows;
        }
    }

    private class DeviceRow {
        private final Device device;
        private final javafx.beans.property.SimpleStringProperty customer;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty model;
        private final javafx.beans.property.SimpleStringProperty serial;
        private final javafx.beans.property.SimpleStringProperty imei;

        private DeviceRow(Device device) {
            this.device = device;
            this.customer = new javafx.beans.property.SimpleStringProperty(customerDisplayName(device.getCustomer()));
            this.type = new javafx.beans.property.SimpleStringProperty(deviceTypeLabel(device.getDeviceType()));
            String brand = valueOrEmpty(device.getBrand());
            String modelName = valueOrEmpty(device.getModel());
            this.model = new javafx.beans.property.SimpleStringProperty((brand + " " + modelName).trim());
            this.serial = new javafx.beans.property.SimpleStringProperty(displayValue(device.getSerialNumber()));
            this.imei = new javafx.beans.property.SimpleStringProperty(displayValue(device.getImei()));
        }

        private Device device() {
            return device;
        }

        private String searchText() {
            return String.join(" ", customer.get(), type.get(), model.get(), serial.get(), imei.get());
        }

        private javafx.beans.property.StringProperty customerProperty() {
            return customer;
        }

        private javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        private javafx.beans.property.StringProperty modelProperty() {
            return model;
        }

        private javafx.beans.property.StringProperty serialProperty() {
            return serial;
        }

        private javafx.beans.property.StringProperty imeiProperty() {
            return imei;
        }
    }

    private class ServiceOrderRow {
        private final ServiceOrder order;
        private final javafx.beans.property.SimpleStringProperty number;
        private final javafx.beans.property.SimpleStringProperty customer;
        private final javafx.beans.property.SimpleStringProperty device;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty priority;
        private final javafx.beans.property.SimpleStringProperty received;
        private final javafx.beans.property.SimpleStringProperty balance;

        private ServiceOrderRow(ServiceOrder order) {
            this.order = order;
            this.number = new javafx.beans.property.SimpleStringProperty(displayValue(order.getOrderNumber()));
            this.customer = new javafx.beans.property.SimpleStringProperty(customerDisplayName(order.getCustomer()));
            this.device = new javafx.beans.property.SimpleStringProperty(deviceDisplayName(order.getDevice()));
            this.status = new javafx.beans.property.SimpleStringProperty(serviceOrderStatusLabel(order.getStatus()));
            this.priority = new javafx.beans.property.SimpleStringProperty(serviceOrderPriorityLabel(order.getPriority()));
            this.received = new javafx.beans.property.SimpleStringProperty(order.getReceivedAt() == null ? "-"
                    : order.getReceivedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            this.balance = new javafx.beans.property.SimpleStringProperty(formatCurrency(order.getBalance()));
        }

        private ServiceOrder order() {
            return order;
        }

        private String searchText() {
            return String.join(" ", number.get(), customer.get(), device.get(), status.get(),
                    valueOrEmpty(order.getDeclaredFailure()), valueOrEmpty(order.getDiagnosis()));
        }

        private javafx.beans.property.StringProperty numberProperty() {
            return number;
        }

        private javafx.beans.property.StringProperty customerProperty() {
            return customer;
        }

        private javafx.beans.property.StringProperty deviceProperty() {
            return device;
        }

        private javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        private javafx.beans.property.StringProperty priorityProperty() {
            return priority;
        }

        private javafx.beans.property.StringProperty receivedProperty() {
            return received;
        }

        private javafx.beans.property.StringProperty balanceProperty() {
            return balance;
        }
    }

    private static class BackupRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty size;

        private BackupRow(Path path, long modifiedAt, long bytes) {
            this.name = new javafx.beans.property.SimpleStringProperty(path.getFileName().toString());
            this.date = new javafx.beans.property.SimpleStringProperty(
                    java.time.Instant.ofEpochMilli(modifiedAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            this.size = new javafx.beans.property.SimpleStringProperty(formatByteSize(bytes));
        }

        private static String formatByteSize(long bytes) {
            if (bytes < 1024) {
                return bytes + " B";
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            }
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }

        private javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        private javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        private javafx.beans.property.StringProperty sizeProperty() {
            return size;
        }
    }

    private class ReportRow {
        private final LocalDateTime dateValue;
        private final BigDecimal amountValue;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty reference;
        private final javafx.beans.property.SimpleStringProperty subject;
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty amount;

        private ReportRow(LocalDateTime date, String type, String reference, String subject,
                          String status, BigDecimal amount) {
            this.dateValue = date;
            this.amountValue = amount == null ? BigDecimal.ZERO : amount;
            this.date = new javafx.beans.property.SimpleStringProperty(date == null ? ""
                    : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            this.type = new javafx.beans.property.SimpleStringProperty(type == null ? "" : type);
            this.reference = new javafx.beans.property.SimpleStringProperty(reference == null ? "" : reference);
            this.subject = new javafx.beans.property.SimpleStringProperty(subject == null ? "" : subject);
            this.status = new javafx.beans.property.SimpleStringProperty(status == null ? "" : status);
            this.amount = new javafx.beans.property.SimpleStringProperty(formatCurrency(this.amountValue));
        }

        private LocalDateTime dateValue() {
            return dateValue;
        }

        private BigDecimal amountValue() {
            return amountValue;
        }

        private String searchText() {
            return String.join(" ", type.get(), reference.get(), subject.get(), status.get(), amount.get());
        }

        private List<String> csvValues() {
            return List.of(date.get(), type.get(), reference.get(), subject.get(), status.get(), amount.get());
        }

        private javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        private javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        private javafx.beans.property.StringProperty referenceProperty() {
            return reference;
        }

        private javafx.beans.property.StringProperty subjectProperty() {
            return subject;
        }

        private javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        private javafx.beans.property.StringProperty amountProperty() {
            return amount;
        }
    }

    private class CustomerRow {
        private final Customer customer;
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty document;
        private final javafx.beans.property.SimpleStringProperty phone;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty location;

        private CustomerRow(Customer customer) {
            this.customer = customer;
            this.name = new javafx.beans.property.SimpleStringProperty(customerDisplayName(customer));
            this.type = new javafx.beans.property.SimpleStringProperty(
                    customer.getCustomerType() == CustomerType.COMPANY ? "Empresa" : "Persona");
            this.document = new javafx.beans.property.SimpleStringProperty(displayValue(customer.getDocument()));
            this.phone = new javafx.beans.property.SimpleStringProperty(displayValue(customer.getPhone()));
            this.email = new javafx.beans.property.SimpleStringProperty(displayValue(customer.getEmail()));
            this.location = new javafx.beans.property.SimpleStringProperty(displayLocation(customer));
        }

        private Customer customer() {
            return customer;
        }

        private String searchText() {
            return String.join(" ", name.get(), type.get(), document.get(), phone.get(), email.get(), location.get());
        }

        private javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        private javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        private javafx.beans.property.StringProperty documentProperty() {
            return document;
        }

        private javafx.beans.property.StringProperty phoneProperty() {
            return phone;
        }

        private javafx.beans.property.StringProperty emailProperty() {
            return email;
        }

        private javafx.beans.property.StringProperty locationProperty() {
            return location;
        }
    }

    private static class DashboardRow {
        private final javafx.beans.property.SimpleStringProperty code;
        private final javafx.beans.property.SimpleStringProperty total;
        private final javafx.beans.property.SimpleStringProperty status;

        private DashboardRow(String code, String total, String status) {
            this.code = new javafx.beans.property.SimpleStringProperty(code);
            this.total = new javafx.beans.property.SimpleStringProperty(total);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty codeProperty() {
            return code;
        }

        public javafx.beans.property.StringProperty totalProperty() {
            return total;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }
}
