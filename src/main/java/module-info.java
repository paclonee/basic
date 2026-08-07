module com.example.personalexpensemanager {
  requires javafx.controls;
  requires javafx.fxml;

  requires org.controlsfx.controls;
  requires org.kordamp.ikonli.javafx;

  requires com.google.gson;

  opens com.example.personalexpensemanager to javafx.fxml;
  opens com.example.personalexpensemanager.controller to javafx.fxml;

  exports com.example.personalexpensemanager;
  exports com.example.personalexpensemanager.controller;
  exports com.example.personalexpensemanager.enums;
  exports com.example.personalexpensemanager.model;
  exports com.example.personalexpensemanager.storage;
  exports com.example.personalexpensemanager.manager;
  exports com.example.personalexpensemanager.view;
  exports com.example.personalexpensemanager.util;
}
