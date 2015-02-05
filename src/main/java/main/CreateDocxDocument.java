package main;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rest on 12/22/14.
 */
public class CreateDocxDocument {
    public static void main(String[] args) throws Docx4JException {
        Map<String, String> map = new HashMap<>();
        map.put("type", "Системный блок");
        map.put("name", "AMD Athlon X3 445 3,1Гц, 4Гб,  мышка, клавиатура");
        map.put("ean", "1234567890123");
        map.put("code", "1154684");

        Map<String, String> map2 = new HashMap<>();
        map2.put("type", "Монитор");
        map2.put("name", "Acer V173");
        map2.put("ean", "343452345234");
        map2.put("code", "44534");

        Map<String, String> map3 = new HashMap<>();
        map3.put("type", "МФУ");
        map3.put("name", "HP1132");
        map3.put("ean", "233452345234");
        map3.put("code", "456334");

        List<Map<String, String>> objects = new ArrayList<>();
        objects.add(map);
        objects.add(map2);
        objects.add(map3);

        new CreateDocxDocument().createAndPrintUserCard("Василий Сергеич", "Антор Карлович", "102", "Образовательной медицины",objects);
    }

    public void createAndPrintUserCard(String giver,String taker, String room,String department,List<Map<String,String>> objects){
        WordprocessingMLPackage wordMLPackage = null;
        try {
            wordMLPackage = WordprocessingMLPackage.createPackage();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        wordMLPackage.getMainDocumentPart().addStyledParagraphOfText("Title", "Карточка учета ОС");
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Отдел: "+department);
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Кабинет: " + room);
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Передающий: " + giver);
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Принимающий: " + taker);
        wordMLPackage.getMainDocumentPart().addStyledParagraphOfText("Heading1", "Оборудование: ");

        int i=1;
        for(Map<String,String> map : objects){
            wordMLPackage.getMainDocumentPart().addStyledParagraphOfText("Heading3", i+". "+map.get("type")+" (Код:"+map.get("code")+" Штрих-код:"+map.get("ean")+")");
            wordMLPackage.getMainDocumentPart().addParagraphOfText("Комплектация: " + map.get("name"));
//            wordMLPackage.getMainDocumentPart().addParagraphOfText("Номер: " + map.get("code"));
//            wordMLPackage.getMainDocumentPart().addParagraphOfText("Штрих код: "+map.get("ean"));
            i++;
        }

        wordMLPackage.getMainDocumentPart().addParagraphOfText("");
        wordMLPackage.getMainDocumentPart().addParagraphOfText("");
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Дата: \t\t\t\t\t\t\t\t\t" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Передал (подпись)\t\t\t\t\t\t\t___________");
        wordMLPackage.getMainDocumentPart().addParagraphOfText("Принял  (подпись)\t\t\t\t\t\t\t___________");

        try {
            File file = new File("report/report-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date()) + ".docx");
            wordMLPackage.save(file);
            ConvertDocxToPDFAndPrint.convertAndPrint(file);
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }


}
