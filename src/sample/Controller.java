package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import operations.Converter;
import operations.XMLwork;
import operations.HistoryListData;
import javafx.fxml.Initializable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {
    private String valute1;
    private String valute2;
    private double amount;
    private double res;
    private String dateChoice[] = new String[120];
    private String date[] = new String[120];
    private int counter = 0;
    private Converter conv = XMLwork.start();
    private Converter currentConverter = conv; // буде змінюватися, коли ми вибираємо нову дату для конвертуваня
    Converter converters[];
    private List<String> alphabetValues = new ArrayList<String>();

    private boolean isDateChanged = false; // перевіряє, чи дата була змінена

    private boolean isValute1Changed = false; // перевіряє, чи була замінена валюта
    private boolean isValute2Changed = false; // те саме

    private static HistoryListData[] listData;
    int index; // індекс для history
    private boolean isHistoryCleared = false; // перевіряє, чи була очищена історія
    private boolean historyHidden = false; // перевіряє, чи була схована історія
    private int indexOfHistoryItem = 0;
    private String changedTo;

    private boolean isСonnected = false; // керує офлайн-режимом

    private boolean isSorted = false;

    private boolean isDateTypeChanged = false;

    private Converter tmpConverter;
    private String tmp[];

    @FXML
    private MenuItem closeWindowMI;
    @FXML
    private MenuItem updateConnection;
    @FXML
    private ChoiceBox chValute1;
    @FXML
    private ChoiceBox chValute2;
    @FXML
    private ChoiceBox chConverter;
    @FXML
    private Button сonvert;
    @FXML
    private TextField tValute1Amount;
    @FXML
    private TextField tConvertedAmount;
    @FXML
    private ListView history;
    @FXML
    private Label historyL;
    @FXML
    private Label lastDateL;
    @FXML
    private TextField dateField;
    @FXML
    private MenuItem changeDateType;


    //виконується при запуску
    public void initialize(URL location, ResourceBundle resources) {
        showUseWarning();
        updateConnection.setDisable(true);
        dateField.setVisible(false);
        dateChooseConverter();
        getValues();
        setEvents();
        listData = new HistoryListData[50];
        closeWindowMI.setOnAction(event1 -> Platform.exit());
    }

    private void setEvents() {
        //показує, що дата була змінена
        chConverter.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                isDateChanged = true;
            }
        });
        history.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                index = history.getItems().size() - history.getSelectionModel().getSelectedIndex() - 1;
                if(chValute1.getValue() != listData[index].historyValute1 || chValute2.getValue() !=
                        listData[index].historyValute2 || chConverter.getValue() != listData[index].historyDate &&
                        !isDateTypeChanged || dateField.getText() != listData[index].historyDate && isDateTypeChanged) {
                    changedTo = "";
                    Alert dataChanged = new Alert(Alert.AlertType.INFORMATION);
                    dataChanged.setTitle("Значення замінені");
                    dataChanged.setHeaderText("Ви змінили валюти і/або дату");
                    if (chValute1.getValue() != listData[index].historyValute1) {
                        changedTo += "Валюта " + chValute1.getValue() + " була замінена на валюту " + listData[index].historyValute1 + "\n";
                    }
                    if (chValute2.getValue() != listData[index].historyValute2) {
                        changedTo += "Валюта " + chValute2.getValue() + " була замінена на валюту " + listData[index].historyValute2 + "\n";
                    }
                    if (chConverter.getValue() != listData[index].historyDate && !isDateTypeChanged) {
                        changedTo += "Дата " + chConverter.getValue() + " була змінена на " + listData[index].historyDate;
                    }
                    if (dateField.getText() != listData[index].historyDate && isDateTypeChanged) {
                        changedTo += "Дата " + dateField.getText() + " була змінена на " + listData[index].historyDate;
                    }
                    System.out.println(changedTo);
                    dataChanged.setContentText(changedTo);
                    dataChanged.show();
                }
                if (index < 50) {
                    chValute1.setValue(listData[index].historyValute1);
                    chValute2.setValue(listData[index].historyValute2);
                    if (!isDateTypeChanged) {
                        chConverter.setValue(listData[index].historyDate);
                    } else dateField.setText(listData[index].historyDate);

                } else {
                    showHistoryFilledWarning();
                }
            }
        });
    }

    @FXML
    private void setDateField() {
        String value;
        Double res1;

        try{
            if(checkDateField()) {
                value = tmp[2] + tmp[1] + tmp[0];
                tmpConverter = XMLwork.createConverter(value);
                res1 = tmpConverter.getConvertedValuteAmount((String) chValute1.getValue(), (String) chValute2.getValue(), Math.abs(Double.parseDouble(tValute1Amount.getText())));
                DecimalFormat f = new DecimalFormat("0.00");
                if (!historyHidden) {
                    addToHistory();
                } //якщо історія вимкнута - оперативна пам'ять не засоряється
                tValute1Amount.setText(String.valueOf(Math.abs(Double.parseDouble(tValute1Amount.getText()))));
                tConvertedAmount.setText(String.valueOf(f.format(res1)));


            }
            else {
                alertForDate();
            }
        } catch (Exception e) {
            alertForDate();
        }
    }
    private boolean checkDateField() {
        boolean ok = false;
        if (Objects.equals(dateField.getText(), "") || (dateField.getText().length() != 10)) {

        } else {
            try {
                tmp = dateField.getText().split("\\.");
                if (Integer.parseInt(tmp[2]) <= 2018 && Integer.parseInt(tmp[2]) >= 2008) {
                    if (Integer.parseInt(tmp[1]) <= 12 && Integer.parseInt(tmp[1]) >= 1) {
                        if (Integer.parseInt(tmp[0]) <= 28 && Integer.parseInt(tmp[0]) >= 1) {
                            ok = true;
                        } else {
                            ok = false;
                        }
                    } else {
                        ok = false;
                    }
                } else {
                    ok = false;
                }
            } catch (Exception e) {
                ok = false;
            }
        }
        return ok;
    }

    @FXML
    private void dateSwitch(){
        if(!isDateTypeChanged){
            dateField.setVisible(true);
            chConverter.setVisible(false);
            isDateTypeChanged = true;
        } else {
            dateField.setVisible(false);
            chConverter.setVisible(true);
            isDateTypeChanged = false;
        }
    }

    private void alertForDate() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText("");
        String razer;
        String []steelS = dateChoice[0].split("\\.");
        if(Integer.parseInt(steelS[0]) < 29){
            razer = dateChoice[0];
        } else {
            steelS[0] = "28";
            razer = steelS[0] + "." + steelS[1] + "." + steelS[2];
        }
        alert.setContentText("Вводити можна тільки числа і знак крапки\".\", дату введіть в форматі dd.mm.yyyy, " +
                "де dd <= 28, mm <= 12, yyyy <= 2018 >= 2008. Дата була замінена на " + dateChoice[0]);
        alert.show();
        dateField.setText(razer);
    }

    private void dateChooseConverter() {
        /* Створимо 2 масиви. Один буде з датами, котрі ми запихнемо в choice,
        а інший з ціми ж датами, але у форматі, який потрібен для виклику
        методу XMLwork.createConverter(String date)*/
        currentConverter = conv;
        File file = new File(XMLwork.filepath_kurs_all);
        final long lastModified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfChoice = new SimpleDateFormat("dd.MM.yyyy");

        date[0] = sdf.format(new Date(lastModified));
        dateChoice[0] = sdfChoice.format(new Date(lastModified));

        // Починаючи з поточної дати змінюємо її та записуємо в масиви з датами
        String currentString = dateChoice[0];
        for (int i = 1; i < dateChoice.length; i++) {
            String tmp[] = currentString.split("\\.");
            tmp[0] = "01";
            tmp[1] = String.valueOf(Integer.parseInt(tmp[1]) - 1);
            if (tmp[1].equals("0")) {
                tmp[1] = "12";
                tmp[2] = String.valueOf(Integer.parseInt(tmp[2]) - 1);
            }
            if (Integer.parseInt(tmp[1]) < 10) {
                tmp[1] = "0" + tmp[1];
            }
            dateChoice[i] = tmp[0] + "." + tmp[1] + "." + tmp[2];
            date[i] = tmp[2] + tmp[1] + tmp[0];
            currentString = dateChoice[i];
        }

        // Добавляємо дати в choice
        for (int i = 0; i < dateChoice.length; i++) {
            chConverter.getItems().add(dateChoice[i]);
        }
        chConverter.setValue(dateChoice[0]);
        // Створюємо масив з конвертерами за дати
        converters = new Converter[date.length];
        converters[0] = conv; // нульовим буде конвертер за поточну дату
        for (int i = 1; i < converters.length; i++) {
            try {
                converters[i] = XMLwork.createConverter(date[i]);
            } catch (IOException ex) {

                // перехід в офлайн-режим

                if(isСonnected){
                    Alert errorConnection = new Alert(Alert.AlertType.ERROR);
                    errorConnection.setTitle("Помилка!");
                    errorConnection.setHeaderText(null);
                    errorConnection.setContentText("Не вдалося оновити підключення. Перевірте з'єднання з інтернетом.");
                    Stage stage = (Stage) errorConnection.getDialogPane().getScene().getWindow();
                    stage.setAlwaysOnTop(true);
                    errorConnection.showAndWait();
                }
                isСonnected = false;
                if (!file.exists()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Помилка!");
                    alert.setHeaderText(null);
                    alert.setContentText("Не вдалося отримати файл. Офлайн-режим ввімкнено.\n" +
                            "Для повторного підключення перевірте з'єднання з інтернетом та\n" +
                            "натисніть на \"Поновити з'єднання\" в меню Файл.");
                    alert.showAndWait();
                }
                changeDateType.setDisable(true);
                SimpleDateFormat sdf_file = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                lastDateL.setText("Будуть використані дані, які були оновлені останній раз " + sdf_file.format(new Date(lastModified)));
                chConverter.setDisable(true);
                updateConnection.setDisable(false);
                break;
            }
        }
    }

    @FXML
    public void itemStateChanged() { // змінює список валют при зміні дати
        String chosenDate = (String) chConverter.getValue();
        for (int i = 0; i < date.length; i++) {
            if (chosenDate.equals(dateChoice[i])) {
                chosenDate = date[i];
            }
        }
        for (int i = 0; i < converters.length; i++) {
            if (converters[i].date.equals(chosenDate)) {
                currentConverter = converters[i];
            }
        }
        valute1 = (String) chValute1.getValue();
        valute2 = (String) chValute2.getValue();
        getValues();
        for (int j = 0; j < currentConverter.valutes.length; j++) {
            if (valute1.equals(currentConverter.valutes[j])) {
                chValute1.setValue(valute1);
                isValute1Changed = false;
                break;
            } else {
                chValute1.setValue("Долар США");
                isValute1Changed = true;
            }
        }
        for (int j = 0; j < currentConverter.valutes.length; j++) {
            if (valute2.equals(currentConverter.valutes[j])) {
                chValute2.setValue(valute2);
                isValute2Changed = false;
                break;
            } else {
                chValute2.setValue("Гривня");
                isValute2Changed = true;
            }
        }
        if (tValute1Amount.getText().equals("")) {
            amount = 0;
        } else {
            amount = Double.parseDouble(tValute1Amount.getText());
        }
        if(isValute1Changed || isValute2Changed){
            Alert valuteChanged = new Alert(Alert.AlertType.INFORMATION);
            String changing = "";
            valuteChanged.setTitle("Попередження");
            if(isValute1Changed){
                changing += "Валюта " + valute1 + " була змінена на Долар США\n";
            }
            if(isValute2Changed){
                changing += "Валюта " + valute2 + " була змінена на Гривню";
            }
            if(isValute1Changed && isValute2Changed){
                valuteChanged.setHeaderText("Такі валюти не існують у вибрану дату. Можливо, вони були змінені на інші " +
                        "або за заданий період проводились деномінації");
            } else valuteChanged.setHeaderText("Такої валюти не існує у вибрану дату. Можливо, вона була замінена на іншу " +
                    "або за заданий період провелась деномінація");
            valuteChanged.setContentText(changing);
            valuteChanged.showAndWait();
            isValute1Changed = false;
            isValute2Changed = false;
        }
    }

    //Закидаємо список курсу валют у чекбокси
    private void getValues() {
        chValute1.getItems().clear(); // видалення списку валют задля вписування нових за поточної дати
        chValute2.getItems().clear();

        if(!isSorted){
            for (int i = 0; i < currentConverter.valutes.length; i++) {
                alphabetValues.add(currentConverter.valutes[i]);
            }
            isSorted = true;
        }
        Collections.sort(alphabetValues);
        chValute1.getItems().addAll(alphabetValues);
        chValute2.getItems().addAll(alphabetValues);
    }

    //Заміна валют по кнопці
    @FXML
    private void swapValues() {
        valute1 = (String) chValute1.getValue();
        valute2 = (String) chValute2.getValue();
        chValute1.setValue(valute2);
        chValute2.setValue(valute1);
        doConvert();
    }

    //Виконання конвертації
    public void doConvert() {
        try {
            if(!isDateTypeChanged) {
                if (isDateChanged) { // Якщо дата змінена - підвантажуються курси по цій даті
                    itemStateChanged();
                    isDateChanged = false;
                }
                amount = Double.parseDouble(tValute1Amount.getText());
                valute1 = (String) chValute1.getValue();
                valute2 = (String) chValute2.getValue();

                res = Math.abs(currentConverter.getConvertedValuteAmount(valute1, valute2, amount));
                DecimalFormat f = new DecimalFormat("0.00");
                if (!historyHidden) {
                    addToHistory();
                } //якщо історія вимкнута - оперативна пам'ять не засоряється
                tValute1Amount.setText(String.valueOf(Math.abs(amount)));
                tConvertedAmount.setText(String.valueOf(f.format(res)));
            } else { setDateField(); }
        } catch (Exception e) {
            showAlert();
        }
    }
    // щось пішло не так - виводиться віконце
    private void showAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setContentText("Вводити можна тільки числа і знак крапки\".\"");
        alert.show();
        tValute1Amount.setText("1");
    }

    @FXML
    private void hideHistory() {
        if (!historyHidden) {
            history.setVisible(false);
            historyL.setVisible(false);
            historyHidden = true;
        } else {
            history.setVisible(true);
            historyL.setVisible(true);
            historyHidden = false;
        }
    }

    //Ліствью, у який додається історія перетворень
    private void addToHistory() {
        if(isHistoryCleared){
            counter = 0;
            indexOfHistoryItem = 0;
            isHistoryCleared = false;
        }
        counter++;
        DecimalFormat f = new DecimalFormat("0.00");
        if (!isDateTypeChanged) {
            history.getItems().add(0, (counter + ". " + f.format(Math.abs(amount)) + " " + valute1 + " ------> "
                    + f.format(Math.abs(res)) + " " + valute2 + " - " + chConverter.getValue()));
        } else {
            history.getItems().add(0, (counter + ". " + f.format(Math.abs(amount)) + " " + valute1 + " ------> "
                    + f.format(Math.abs(res)) + " " + valute2 + " - " + dateField.getText()));
        }
        if(counter == 40){
            Alert historyIsCloseToFullAlert = new Alert(Alert.AlertType.WARNING);
            historyIsCloseToFullAlert.setTitle("Попередження!");
            historyIsCloseToFullAlert.setHeaderText("Увага!");
            historyIsCloseToFullAlert.setContentText("Ви не зможете активувати елементи з індексом більше 50.\n" +
                    "Після досягнення індексу в 50, будь-ласка, очистіть історію.");
            historyIsCloseToFullAlert.showAndWait();
        }
        if(counter == 50){
            showHistoryFilledWarning();
        }
        if(counter <= 50) {
            if(!isDateTypeChanged) {
                listData[indexOfHistoryItem] = new HistoryListData(valute1, valute2, (String) chConverter.getValue());
            } else {
                listData[indexOfHistoryItem] = new HistoryListData(valute1, valute2, dateField.getText());
            }
            indexOfHistoryItem++;
        }
    }
    // очищення історії
    @FXML
    private void clearHistory(){
        isHistoryCleared = true;
        history.getItems().clear();
    }
    // дія кнопки перепідключення (доступна тільки в офлайн-режимі
    @FXML
    private void reConnect(){
        isСonnected = true;
        dateChooseConverter();
        if(isСonnected){
            changeDateType.setDisable(false);
            chConverter.setDisable(false);
            updateConnection.setDisable(true);
            lastDateL.setText("З'єднання поновлено.");
        }
    }
    // показати вікно "Про нас"
    @FXML
    private void showAboutWindow() throws Exception {
        Stage aboutStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("about.fxml"));
        aboutStage.setTitle("Хто чим займався?");
        aboutStage.getIcons().add(new Image("sample/badLogo.png"));
        aboutStage.setScene(new Scene(root, 712, 214));
        aboutStage.setResizable(false);
        aboutStage.show();
    }
    // показати графік
    @FXML
    private void showChartWindow() throws Exception {
        String currentDate = null;

        if(isDateTypeChanged && checkDateField()) {
            String tmp[] = dateField.getText().split("\\.");
            currentDate = tmp[2] + tmp[1] + tmp[0];
        } else {
            for (int i = 0; i < dateChoice.length; i++) {
                if (Objects.equals(dateChoice[i], (String) chConverter.getValue())) {
                    currentDate = date[i];
                }
            }
        }
        Stage chartStage = new Stage();
        FXMLLoader loader=new FXMLLoader(getClass().getResource("chart.fxml"));
        Parent root =(Parent) loader.load();
        Controller_chart controller_chart=loader.getController();
        controller_chart.createChart(XMLwork.createConverter(currentDate),(String)chValute1.getValue(),(String)chValute2.getValue());
        chartStage.setTitle("Графік курсів");
        root.getStylesheets().add("sample/chartStyle.css");
        chartStage.getIcons().add(new Image("sample/badLogo.png"));
        chartStage.setScene(new Scene(root,690, 450));
        chartStage.show();
    }
    // якщо історія заповнена - виводити це
    private void showHistoryFilledWarning(){
        Alert historyIsFullAlert = new Alert(Alert.AlertType.WARNING);
        historyIsFullAlert.setTitle("Попередження!");
        historyIsFullAlert.setHeaderText("Увага! Історія переповнена!");
        historyIsFullAlert.setContentText("Ви не можете активувати елементи з індексом більше 50.\n" +
                "Рекомендується очистити історію");
        Stage stage = (Stage) historyIsFullAlert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        historyIsFullAlert.showAndWait();
    }
    // довідка
    @FXML
    private void showUseWarning(){
        Alert useWarning = new Alert(Alert.AlertType.INFORMATION);
        useWarning.setTitle("Довідка");
        useWarning.setHeaderText("Користуючись програмою, вам слід розуміти наступне");
        useWarning.setContentText("1. Вводити можна тільки числа і знак крапки\".\".\n" +
                "2. Курс може не збігатися з курсами з інших програм\\сайтів.\n" +
                "3. Список валют після зміни дати змінюється тільки після першого конвертування.\n" +
                "4. Якщо валюта, яка існувала у минулому вибраному році, виявиться відсутньою в" +
                "поточному році, виведеться окреме інформаційне вікно.\n" +
                "5. Графік показує курс за вибрану дату, за місяць від неї та " +
                "оновлюється тільки при повторному натиску пункту меню \"Показати графік\". " +
                "Для деяких валют курс оновлюється лише раз на місяць, тому ви можете спостерігати" +
                "рівні лінії напротязі місяця.\n" +
                "6. Історія має функціонал. Виберіть конвертацію з історії для заповнення дати та курсів " +
                "з неї у списки дати та курсів. Ви не можете користуватись цим функціоналом, якщо " +
                "номер елемента перевищує 50. Тоді рекомендовано очистити історію для подальшого " +
                "використання. Також ви можете сховати історію і не записувати" +
                "в неї усі конвертації, натиснувши \"Сховати історію\".\n" +
                "7. Сортування по алфавіту працює не правильно. Першими двома літерами алфавіту " +
                "вважаються \'Є\' та \'І\', а потім уже як ми звикли.\n" +
                "8. Всі дані беруться через API з сайту НБУ. Можливо через це по невідомим нам причинам " +
                "з 01.12.2011 по 01.02.2014 включно при конвертуванні Долару США у гривню виходить" +
                "один показник - 7,99. Вини розробників у цьому ми не бачимо. Скоріш за все, це гріх " +
                "НБУ.\n" +
                "9. Ви можете вводити свої дати. Для цього в Меню --> Файл виберіть пункт " +
                " \"Тип введення дати\" і вводьте своє значення. При цьому ви маєте тримати дати в " +
                "проміжку від 2008 до 2018 року і вводити число місяця до 28.");
        Stage stage = (Stage) useWarning.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        useWarning.show();
    }
}