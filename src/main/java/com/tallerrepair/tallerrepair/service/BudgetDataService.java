package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Budget;
import com.tallerrepair.tallerrepair.entity.Customer;
import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.entity.Payment;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.entity.User;
import com.tallerrepair.tallerrepair.enums.CustomerType;
import com.tallerrepair.tallerrepair.enums.DeviceType;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import com.tallerrepair.tallerrepair.enums.ServiceOrderPriority;
import com.tallerrepair.tallerrepair.enums.ServiceOrderStatus;
import com.tallerrepair.tallerrepair.repository.BudgetRepository;
import com.tallerrepair.tallerrepair.repository.CustomerRepository;
import com.tallerrepair.tallerrepair.repository.DeviceRepository;
import com.tallerrepair.tallerrepair.repository.PaymentRepository;
import com.tallerrepair.tallerrepair.repository.ServiceOrderRepository;
import com.tallerrepair.tallerrepair.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDataService {

    private final BudgetRepository budgetRepository = new BudgetRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final DeviceRepository deviceRepository = new DeviceRepository();
    private final ServiceOrderRepository serviceOrderRepository = new ServiceOrderRepository();
    private final UserRepository userRepository = new UserRepository();

    public void ensureDemoBudgetData() {
        if (budgetRepository.countAll() > 0L) {
            return;
        }

        Optional<User> adminUser = userRepository.findByUsername("admin");

        List<DemoBudgetBundle> bundles = new ArrayList<>();
        bundles.add(new DemoBudgetBundle(
                "Ana García",
                "48123456Z",
                "OT-000101",
                "Cambio de pantalla y revisión de batería",
                new BigDecimal("520.00"),
                new BigDecimal("420.00"),
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("370.00"),
                "Aprobado"
        ));
        bundles.add(new DemoBudgetBundle(
                "Luis Mendoza",
                "12345678A",
                "OT-000102",
                "Reinstalación de sistema y limpieza térmica",
                new BigDecimal("840.00"),
                new BigDecimal("680.00"),
                new BigDecimal("160.00"),
                new BigDecimal("300.00"),
                new BigDecimal("540.00"),
                "Pendiente"
        ));
        bundles.add(new DemoBudgetBundle(
                "Sofía Ruiz",
                "87654321B",
                "OT-000103",
                "Diagnóstico de carga y reemplazo de conector",
                new BigDecimal("690.00"),
                new BigDecimal("560.00"),
                new BigDecimal("130.00"),
                new BigDecimal("250.00"),
                new BigDecimal("440.00"),
                "Aprobado"
        ));

        for (DemoBudgetBundle bundle : bundles) {
            Customer customer = customerRepository.findByDocument(bundle.document()).orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setCustomerType(CustomerType.PERSON);
                String[] parts = bundle.customerName().split(" ", 2);
                newCustomer.setFirstName(parts[0]);
                newCustomer.setLastName(parts.length > 1 ? parts[1] : "");
                newCustomer.setDocument(bundle.document());
                newCustomer.setPhone("+34 600 000 000");
                newCustomer.setEmail((parts[0] + "." + (parts.length > 1 ? parts[1] : "")).toLowerCase().replace(" ", "") + "@mail.com");
                newCustomer.setCity("Sevilla");
                newCustomer.setProvince("Sevilla");
                newCustomer.setActive(true);
                return customerRepository.save(newCustomer);
            });

            Device device = deviceRepository.findByCustomerId(customer.getId()).stream().findFirst().orElseGet(() -> {
                Device newDevice = new Device();
                newDevice.setCustomer(customer);
                newDevice.setDeviceType(DeviceType.CELULAR);
                newDevice.setBrand("Samsung");
                newDevice.setModel("Galaxy A54");
                newDevice.setSerialNumber("SN-" + bundle.document().substring(0, 5).toUpperCase());
                newDevice.setColor("Negro");
                newDevice.setAccessories("Cargador original");
                newDevice.setPhysicalCondition("Bastante correcto");
                return deviceRepository.save(newDevice);
            });

            ServiceOrder serviceOrder = serviceOrderRepository.findByOrderNumber(bundle.orderNumber()).orElseGet(() -> {
                ServiceOrder newOrder = new ServiceOrder();
                newOrder.setOrderNumber(bundle.orderNumber());
                newOrder.setCustomer(customer);
                newOrder.setDevice(device);
                newOrder.setTechnician(adminUser.orElse(null));
                newOrder.setReceivedAt(LocalDateTime.now().minusDays(3));
                newOrder.setEstimatedDeliveryAt(LocalDate.now().plusDays(5));
                newOrder.setStatus(ServiceOrderStatus.DIAGNOSIS);
                newOrder.setDeclaredFailure(bundle.declaredFailure());
                newOrder.setDiagnosis(bundle.declaredFailure());
                newOrder.setPriority(ServiceOrderPriority.NORMAL);
                newOrder.setEstimatedCost(bundle.estimatedCost());
                newOrder.setTotal(bundle.total());
                newOrder.setPaid(bundle.paid());
                newOrder.setBalance(bundle.balance());
                newOrder.setNotes("Presupuesto registrado desde semilla inicial");
                return serviceOrderRepository.save(newOrder);
            });

            Budget budget = new Budget();
            budget.setServiceOrder(serviceOrder);
            budget.setBudgetNumber("BUD-" + String.format("%06d", bundles.indexOf(bundle) + 1));
            budget.setIssuedAt(LocalDateTime.now().minusDays(2 - bundles.indexOf(bundle)));
            budget.setStatus("Aprobado".equals(bundle.status()) ? com.tallerrepair.tallerrepair.enums.BudgetStatus.APPROVED : com.tallerrepair.tallerrepair.enums.BudgetStatus.PENDING);
            budget.setPartsCost(bundle.partsCost());
            budget.setLaborCost(bundle.laborCost());
            budget.setDiscount(bundle.discount());
            budget.setSurcharge(bundle.surcharge());
            budget.setTotal(bundle.total());
            budget.setNotes(bundle.declaredFailure());
            budgetRepository.save(budget);

            Payment payment = new Payment();
            payment.setServiceOrder(serviceOrder);
            payment.setBudget(budget);
            payment.setAmount(bundle.advance());
            payment.setPaymentMethod(PaymentMethod.CARD);
            payment.setReferenceNumber("REF-" + bundle.orderNumber().replace("OT-", ""));
            payment.setReceivedAt(LocalDateTime.now().minusDays(1));
            payment.setNotes("Cobro inicial del presupuesto");
            paymentRepository.save(payment);

            serviceOrder.setPaid(bundle.paid());
            serviceOrder.setBalance(bundle.balance());
            serviceOrder.setTotal(bundle.total());
            serviceOrderRepository.save(serviceOrder);
        }
    }

    public List<Budget> getRecentBudgets(int limit) {
        return budgetRepository.findRecent(limit);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAllWithDetails();
    }

    public List<Payment> getPaymentsForBudget(Budget budget) {
        if (budget == null || budget.getId() == null) {
            return List.of();
        }
        return paymentRepository.findByBudgetId(budget.getId());
    }

    private record DemoBudgetBundle(
            String customerName,
            String document,
            String orderNumber,
            String declaredFailure,
            BigDecimal total,
            BigDecimal partsCost,
            BigDecimal laborCost,
            BigDecimal advance,
            BigDecimal balance,
            String status
    ) {
        BigDecimal discount() {
            return total.subtract(partsCost.add(laborCost)).max(BigDecimal.ZERO);
        }

        BigDecimal surcharge() {
            return BigDecimal.ZERO;
        }

        BigDecimal estimatedCost() {
            return partsCost.add(laborCost);
        }

        BigDecimal paid() {
            return advance;
        }
    }
}
