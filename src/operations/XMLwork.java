package operations;

/*
 * date: 19.05.2018
 * version: 3
 */
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import javafx.scene.control.Alert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLwork {

    public static String url_kurs_all = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange";
    public static String filepath_kurs_all = "XML_data" + File.separator + "allKurs.xml"; // Путь к файлу

    /* В цій функції ми виконуємо всі необхідні дії для створення xml файла та
    масивів назв валют і курсів. Функція повертає об'єкт Converter, який надалі
    буде використовуватися для обчислень.*/
    public static Converter start() {

        // Намагаємося створити\оновити файл xml з курсами всіх валют
        try {
            XMLwork.createXmlAllKursFile();
        } catch (IOException | TransformerException ex) {
            // Якщо не вдалося оновити дані, то виконуємо наступні дії
            System.out.println("Не вдалося оновити курси валют.");
            File file = new File(XMLwork.filepath_kurs_all);
            if (!file.exists()) {
                Alert noFileAlert = new Alert(Alert.AlertType.ERROR);
                noFileAlert.setTitle("Помилка!");
                noFileAlert.setHeaderText("Локальний файл з курсами валют не знайдено!");
                noFileAlert.setContentText("Під'єднайтесь до " +
                        "інтернету і спробуйте знову запустити програму. Без локального файлу програма виконуватись " +
                        "не буде.\n Якщо при підключенні до інтернету програма і надалі не запускається - спробуйте " +
                        "видалити папку \"XML_data\" і заново відкрити програму.");
                noFileAlert.showAndWait();
                System.out.println("Локальний файл з курсами валют не знайдено."
                        + "Виконання програми неможливе.");
                System.exit(0);
                // Тут вставимо вклик метода який виведе це повідомлення в графічному інтерфейсі
            }
            final long lastModified = file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            System.out.println("Будуть використані дані, які були оновлені останній раз " + sdf.format(new Date(lastModified)));
        }

        Document docAllKurs = XMLwork.createXmlAllKursDocument(); // Створюємо документ на основі файла allKurs.xml
        NodeList nodeListOfValutes = docAllKurs.getElementsByTagName("txt"); // Шукаємо по файлу всі значення всіх тегів txt, які містять назви валют, і записуємо їх у вигляді NodeList

        // Тепер для зручності подальшої роботи перетворимо NodeList в звичайний масив
        String valutes[] = new String[nodeListOfValutes.getLength() + 1];
        for (int i = 0; i < nodeListOfValutes.getLength(); i++) {
            valutes[i] = nodeListOfValutes.item(i).getTextContent();
        }
        // Тепер створимо масив самих курсів
        NodeList nodeListOfKurses = docAllKurs.getElementsByTagName("rate");
        double kurses[] = new double[nodeListOfKurses.getLength() + 1];
        for (int i = 0; i < nodeListOfKurses.getLength(); i++) {
            kurses[i] = Double.parseDouble(nodeListOfKurses.item(i).getTextContent());
        }

        valutes[nodeListOfValutes.getLength()] = "Гривня";
        kurses[nodeListOfKurses.getLength()] = 1;

        /* Створюємо об'єкт Converter, який при конвертуванні буде використовувати
        дані з створених масивів. Також ці масиви потім будуть використанні для
        виводу назв валют в списках в інтерфейсі*/
        Converter conv = new Converter(valutes, kurses);
        return conv;
    }

    // Створюємо xml файл на основі створенного документа по url
    public static void createXmlAllKursFile() throws MalformedURLException, IOException, TransformerConfigurationException, TransformerException {
        URL xmlUrl = new URL(url_kurs_all);
        InputStream in = xmlUrl.openStream();
        Document doc = parse(in); // Створили документ з даними по всім курсам

        // Далі перетворюємо цей документ в файл xml
        DOMSource source = new DOMSource(doc);
        File fileDir = new File("XML_data");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        FileOutputStream writer = new FileOutputStream(new File(filepath_kurs_all));
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }

    // Створюємо документ з xml файла
    public static Document createXmlAllKursDocument() {
        Document ret = null;
        File file = new File(filepath_kurs_all);

        DocumentBuilder b;
        try {
            b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ret = b.parse(file);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            System.err.println("unable to create Document from XML file: " + ex);
        }
        return ret;
    }

    // Створює документ
    public static Document parse(InputStream is) {
        Document ret = null;
        DocumentBuilderFactory domFactory;
        DocumentBuilder builder;

        try {
            domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            domFactory.setNamespaceAware(false);
            builder = domFactory.newDocumentBuilder();

            ret = builder.parse(is);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            System.err.println("unable to load XML: " + ex);
        }
        return ret;
    }

    // Повертає об'єкт Converter, котрий містить дані по курсам валют за вказану дату
    // date повинен бути у форматі yyyyMMdd (20180519)
    public static Converter createConverter(String date) throws MalformedURLException, IOException {

        String url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=" + date;

        URL xmlUrl = new URL(url);
        InputStream in = xmlUrl.openStream();
        Document doc = parse(in); // Створили документ з даними по всім курсам        

        NodeList nodeListOfValutes = doc.getElementsByTagName("txt"); // Шукаємо по файлу всі значення всіх тегів txt, які містять назви валют, і записуємо їх у вигляді NodeList

        // Тепер для зручності подальшої роботи перетворимо NodeList в звичайний масив
        String valutes[] = new String[nodeListOfValutes.getLength() + 1];
        for (int i = 0; i < nodeListOfValutes.getLength(); i++) {
            valutes[i] = nodeListOfValutes.item(i).getTextContent();
        }
        // Тепер створимо масив самих курсів
        NodeList nodeListOfKurses = doc.getElementsByTagName("rate");
        double kurses[] = new double[nodeListOfKurses.getLength() + 1];
        for (int i = 0; i < nodeListOfKurses.getLength(); i++) {
            kurses[i] = Double.parseDouble(nodeListOfKurses.item(i).getTextContent());
        }

        valutes[nodeListOfValutes.getLength()] = "Гривня";
        kurses[nodeListOfKurses.getLength()] = 1;

        /* Створюємо об'єкт Converter */
        Converter conv = new Converter(valutes, kurses, date);
        return conv;
    }
}
