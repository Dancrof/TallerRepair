package com.tallerrepair.tallerrepair.service;

import com.tallerrepair.tallerrepair.entity.SystemSetting;
import com.tallerrepair.tallerrepair.repository.SystemSettingRepository;

import java.util.ArrayList;
import java.util.List;

public class SystemSettingsService {

    private static final String CATEGORY = "company";
    private final SystemSettingRepository repository = new SystemSettingRepository();

    public CompanyProfile getCompanyProfile() {
        return new CompanyProfile(
                getValue("company.name", "TallerRepair"),
                getValue("company.tax_id", ""),
                getValue("company.phone", ""),
                getValue("company.email", ""),
                getValue("company.address", "")
        );
    }

    public void saveCompanyProfile(CompanyProfile profile) {
        if (profile == null || clean(profile.name()) == null) {
            throw new IllegalArgumentException("El nombre del taller es obligatorio.");
        }

        List<SystemSetting> settings = new ArrayList<>();
        settings.add(setting("company.name", clean(profile.name()), "Nombre comercial del taller"));
        settings.add(setting("company.tax_id", clean(profile.taxId()), "Identificación fiscal"));
        settings.add(setting("company.phone", clean(profile.phone()), "Teléfono del taller"));
        settings.add(setting("company.email", clean(profile.email()), "Correo del taller"));
        settings.add(setting("company.address", clean(profile.address()), "Dirección del taller"));
        repository.saveAll(settings);
    }

    private String getValue(String key, String defaultValue) {
        return repository.findByKey(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    private SystemSetting setting(String key, String value, String description) {
        SystemSetting setting = repository.findByKey(key).orElseGet(SystemSetting::new);
        setting.setKey(key);
        setting.setValue(value == null ? "" : value);
        setting.setDescription(description);
        setting.setCategory(CATEGORY);
        return setting;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CompanyProfile(String name, String taxId, String phone, String email, String address) {
    }
}