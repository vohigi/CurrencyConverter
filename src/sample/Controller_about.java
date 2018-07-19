package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class Controller_about{
    @FXML
    private void showDonateInfo(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Серйозно?");
        alert.setHeaderText("Донат?");
        alert.setContentText("Ви захотіли закинути цим хлопцям трішки грошей.\n" +
                "Краще купіть собі мівінки. Або багато мівінок. Або збирайте на макарошки.");
        alert.showAndWait();
    }
}
