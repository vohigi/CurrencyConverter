package operations;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

// updated 19.05.2018

public class Converter {

    public final String valutes[]; // масив з назвами валют
    public final double kurses[]; // масив з курсами валют
    public final String date; // дата (yyyyMMdd)

    Converter(String[] valutes, double[] kurses) {
        this.kurses = kurses;
        this.valutes = valutes;

        File file = new File(XMLwork.filepath_kurs_all);
        final long lastModified = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        this.date = sdf.format(new Date(lastModified));
    }

    Converter(String[] valutes, double[] kurses, String date) {
        this.kurses = kurses;
        this.valutes = valutes;
        this.date = date;
    }

    /* В цю функцію ми будемо передавати назви валют (з якої в яку конвертується),
    а також суму яка конвертується. Всі ці три параметри будуть братися з компонентів
    графічного інтерфейсу і записуватися в змінні, які потім передадуться у цю
    функцію. Функцію будемо викликати при натиску кнопки конвертування, мабуть.
    Результат будемо записувати в компонент, який відповідатиме за результат
    конвертування.*/
    public double getConvertedValuteAmount(String itWillBeConverted, String inWhatWillBeConverted, double valute1Amount) {
        int index1 = 0;
        int index2 = 0;
        for (int i = 0; i < valutes.length; i++) {
            if (valutes[i].equals(itWillBeConverted)) {
                index1 = i;
            }
            if (valutes[i].equals(inWhatWillBeConverted)) {
                index2 = i;
            }
        }
        return (valute1Amount * kurses[index1]) / kurses[index2];
    }
}
