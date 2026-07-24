module com.example.personalexpensemanager {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.example.personalexpensemanager to javafx.fxml;
    exports com.example.personalexpensemanager;
}