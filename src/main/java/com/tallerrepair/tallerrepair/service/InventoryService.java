package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.InventoryMovement;
import com.tallerrepair.tallerrepair.entity.Product;
import com.tallerrepair.tallerrepair.enums.InventoryMovementType;

public class InventoryService {

    public Product recordMovement(Product product, int quantity, InventoryMovementType type, String notes) {
        if (product == null) {
            throw new IllegalArgumentException("El producto es requerido.");
        }
        if (type == null) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }

        if (type == InventoryMovementType.OUT && quantity > product.getQuantityOnHand()) {
            throw new IllegalStateException("No hay stock suficiente para esta salida.");
        }

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setNotes(notes);

        switch (type) {
            case IN -> product.adjustStock(quantity);
            case OUT -> product.adjustStock(-quantity);
            case ADJUSTMENT -> product.adjustStock(quantity);
            default -> throw new IllegalStateException("Tipo de movimiento no soportado: " + type);
        }

        product.getInventoryMovements().add(movement);
        return product;
    }
}
