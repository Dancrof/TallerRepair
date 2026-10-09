package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Device;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DeviceDataServiceTest {

    @Test
    void shouldLoadEquipmentWithCustomerAndValidateRequiredModel() {
        new BudgetDataService().ensureDemoBudgetData();
        DeviceDataService service = new DeviceDataService();

        var devices = service.getDevices();
        assertFalse(devices.isEmpty());
        assertNotNull(devices.get(0).getCustomer());

        Device incompleteDevice = new Device();
        incompleteDevice.setCustomer(devices.get(0).getCustomer());
        incompleteDevice.setDeviceType(devices.get(0).getDeviceType());
        assertThrows(IllegalArgumentException.class, () -> service.save(incompleteDevice));
    }
}