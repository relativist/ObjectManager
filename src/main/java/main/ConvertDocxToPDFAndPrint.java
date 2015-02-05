package main;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.awt.Color;
import fr.opensagres.xdocreport.itext.extension.font.ITextFontRegistry;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;

public class ConvertDocxToPDFAndPrint {
    public static void main(String[] args) {

    }

    public static void convertAndPrint(File inFile) {
        long startTime = System.currentTimeMillis();
        String outFileName = "report/report-"+new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date())+".pdf";
        try {
            InputStream in = new FileInputStream(inFile);
//            InputStream in = new FileInputStream(new File("report.docx"));
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);
            IContext context = report.createContext();

            Calendar rightNow = Calendar.getInstance();
            int year = rightNow.get(Calendar.YEAR);
            int month = rightNow.get(Calendar.MONTH) + 1;
            int day = rightNow.get(Calendar.DAY_OF_MONTH);

            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            Date current = new Date();
            String fCurrent = sdFormat.format(current);

            context.put("name", "name");
            context.put("year", year);
            context.put("month", month);
            context.put("day", day);
            context.put("currentDate", fCurrent);

            File outFile = new File(outFileName);
            OutputStream out = new FileOutputStream(outFile);

            PdfOptions pdfOptions = PdfOptions.create();
            pdfOptions.fontProvider(new ITextFontRegistry() {
                public Font getFont(String familyName, String encoding, float size, int style, Color color) {
                    try {
                        BaseFont bfRussian =
                                BaseFont.createFont("fonts/tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        Font fontRussian = new Font(bfRussian, size, style, color);
                        if (familyName != null)
                            fontRussian.setFamily(familyName);
                        return fontRussian;
                    } catch (Throwable e) {
                        e.printStackTrace();
                        return ITextFontRegistry.getRegistry().getFont(familyName, encoding, size, style, color);
                    }
                }

            });

            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF).subOptions(pdfOptions);
            report.convert(context, options, out);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        PrintPdf.printPDF(outFileName, PrintPdf.choosePrinter());
        System.out.println("Generate DocxLettreRelance.pdf with " + (System.currentTimeMillis() - startTime) + " ms.");
    }
}