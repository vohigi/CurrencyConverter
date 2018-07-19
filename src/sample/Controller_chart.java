package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.util.StringConverter;
import operations.Converter;
import operations.XMLwork;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;



public class Controller_chart implements Initializable {
    String dateChoice[] = new String[10];
    String date[] = new String[10];
    Converter converters[];
    double values[];

    @FXML
    private NumberAxis nAxis;
    @FXML
    private CategoryAxis cAxis;
    @FXML
    private LineChart<String, Double> chart1;
    Converter currentConverter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cAxis.setLabel("Дата");
    }

    public void createChart(Converter currentConv, String valute1, String valute2) {
        currentConverter = currentConv;
        nAxis.setAutoRanging(false);
        XYChart.Series series = new XYChart.Series<>();
        String month[] = new String[10];
        dateChooseConverter();
        for (int i = 0; i < month.length; i++) {
            month[i] = dateChoice[i];
        }
        DecimalFormat formatter = new DecimalFormat("#0.00000");
        values=new double[converters.length];
        for (int i = 9; i >= 0; i--) {
            values[i]=converters[i].getConvertedValuteAmount(valute1,valute2,1);
        }
        chart1.getData().addAll(series);
        nAxis.setTickUnit((getMax(values)-getMin(values))/20);
        nAxis.setUpperBound(getMax(values) + nAxis.getTickUnit()*2);
        nAxis.setLowerBound(getMin(values) - nAxis.getTickUnit()*2);
        if(nAxis.getTickUnit()==0){
            nAxis.setAutoRanging(true);
        }
        nAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return(formatter.format(object));
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });
        for (int i = 9; i >= 0; i--) {
            series.getData().add(new XYChart.Data<>(month[i],values[i]));
        }
    }

    private void dateChooseConverter() {
        /* Створимо 2 масиви. Один буде з датами, котрі ми запихнемо в choice,
        а інший з ціми ж датами, але у форматі, який потрібен для виклику
        методу XMLwork.createConverter(String date)*/
        File file = new File(XMLwork.filepath_kurs_all);
        final long lastModified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdfChoice = new SimpleDateFormat("dd.MM.yyyy");

        date[0] = currentConverter.date;
        dateChoice[0] = currentConverter.date.substring(6,8)+"."+currentConverter.date.substring(4,6)+"."+currentConverter.date.substring(0,4);

        // Починаючи з поточної дати змінюємо її та записуємо в масиви з датами
        String currentString = dateChoice[0];
        for (int i = 1; i < dateChoice.length; i++) {
            String tmp[] = currentString.split("\\.");
            tmp[0] = String.valueOf(Integer.parseInt(tmp[0]) - 3);
            if (tmp[0].equals("0") || Integer.parseInt(tmp[0]) < 0 || Integer.parseInt(tmp[0]) > 28) {
                tmp[0]="28";
                tmp[1] = String.valueOf(Integer.parseInt(tmp[1]) - 1);
                if (tmp[1].equals("0")) {
                    tmp[1] = "12";
                    tmp[2] = String.valueOf(Integer.parseInt(tmp[2]) - 1);
                }
                if (Integer.parseInt(tmp[1]) < 10) {
                    tmp[1] = "0" + tmp[1];
                }

            }
            if (Integer.parseInt(tmp[0]) < 10) {
                tmp[0] = "0" + tmp[0];
            }

            dateChoice[i] = tmp[0] + "." + tmp[1] + "." + tmp[2];
            date[i] = tmp[2] + tmp[1] + tmp[0];
            currentString = dateChoice[i];
        }
        converters = new Converter[date.length];
        converters[0] = currentConverter; // нульовим буде конвертер за поточну дату
        for (int i = 1; i < converters.length; i++) {
            try {
                converters[i] = XMLwork.createConverter(date[i]);
            } catch (IOException ex) {
            }
        }
    }
    private double getMax(double[] d){
        double max=0;
        for(double x:d){
            if(x>max)max=x;
        }
        return max;
    }
    private double getMin(double[] d){
        double min=999;
        for(double x:d){
            if(x<min)min=x;
        }
        return min;
    }

}

