package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.Device;
import com.tallerrepair.tallerrepair.repository.DeviceRepository;

import java.util.List;

public class DeviceDataService {

    private final DeviceRepository deviceRepository = new DeviceRepository();

    public List<Device> getDevices() {
        return deviceRepository.findAllWithCustomer();
    }

    public Device save(Device device) {
        if (device == null || device.getCustomer() == null || device.getCustomer().getId() == null) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
        if (device.getDeviceType() == null) {
            throw new IllegalArgumentException("Selecciona el tipo de equipo.");
        }
        device.setBrand(clean(device.getBrand()));
        device.setModel(clean(device.getModel()));
        if (device.getModel() == null) {
            throw new IllegalArgumentException("El modelo del equipo es obligatorio.");
        }
        device.setSerialNumber(clean(device.getSerialNumber()));
        device.setImei(clean(device.getImei()));
        device.setColor(clean(device.getColor()));
        device.setPasswordOrPin(clean(device.getPasswordOrPin()));
        device.setAccessories(clean(device.getAccessories()));
        device.setPhysicalCondition(clean(device.getPhysicalCondition()));
        device.setNotes(clean(device.getNotes()));
        return deviceRepository.save(device);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}