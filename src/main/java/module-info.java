module se.photoproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.graphics;
    requires java.logging; //added this to fix an error, tell me if it works for you too\


    opens se233.photoproject to javafx.fxml;
    exports se233.photoproject;
}