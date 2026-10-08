module com.tallerrepair.tallerrepair {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires org.slf4j;
    requires org.hibernate.orm.core;
    requires org.hibernate.orm.community.dialects;
    requires java.sql;
    requires jakarta.persistence;
    requires jbcrypt;

    opens com.tallerrepair.tallerrepair to javafx.fxml;
    opens com.tallerrepair.tallerrepair.controller to javafx.fxml;
    opens com.tallerrepair.tallerrepair.entity to org.hibernate.orm.core;

    exports com.tallerrepair.tallerrepair;
    exports com.tallerrepair.tallerrepair.controller;
    exports com.tallerrepair.tallerrepair.entity;
    exports com.tallerrepair.tallerrepair.repository;
    exports com.tallerrepair.tallerrepair.service;
    exports com.tallerrepair.tallerrepair.security;
    exports com.tallerrepair.tallerrepair.session;
}