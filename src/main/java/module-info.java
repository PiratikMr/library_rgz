module example.org.library {
    requires javafx.controls;
    requires javafx.fxml;

    opens example.org.library to javafx.fxml;
    opens example.org.library.controller to javafx.fxml;
    opens example.org.library.model to javafx.fxml;

    exports example.org.library;
    exports example.org.library.controller;
    exports example.org.library.model;
    exports example.org.library.service;
    exports example.org.library.repository;
    exports example.org.library.search;
    exports example.org.library.exception;
}