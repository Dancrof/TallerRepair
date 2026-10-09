package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.entity.ServiceOrder;
import com.tallerrepair.tallerrepair.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalClosureServiceTest {

    @Test
    void shouldCloseOrderWithBudgetPaymentAndStockAdjustment() {
        ServiceOrder order = new ServiceOrder();
        order.setOrderNumber("OT-999");
        order.setTotal(new BigDecimal("100.00"));
        order.setPaid(new BigDecimal("0.00"));
        order.setBalance(new BigDecimal("100.00"));

        Product product = new Product();
        product.setSku("P-001");
        product.setName("Pantalla");
        product.setSalePrice(new BigDecimal("20.00"));
        product.setQuantityOnHand(5);

        OperationalClosureService service = new OperationalClosureService();
        OperationalClosureService.OrderClosureResult result = service.closeOrder(
                order,
                new BigDecimal("30.00"),
                new BigDecimal("70.00"),
                new BigDecimal("10.00"),
                "Revisión y cambio de pieza",
                List.of(new OperationalClosureService.ProductUsage(product, 1)),
                new BigDecimal("90.00"),
                PaymentMethod.CARD,
                "REF-CLOSE-001"
        );

        assertNotNull(result);
        assertNotNull(result.budget());
        assertEquals(new BigDecimal("90.00"), result.budget().getTotal());
        assertEquals(new BigDecimal("90.00"), result.payment().getAmount());
        assertEquals(4, product.getQuantityOnHand());
        assertTrue(result.cashImpact().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(new BigDecimal("0.00"), order.getBalance());
    }
}
