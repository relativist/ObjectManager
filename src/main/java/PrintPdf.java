import org.apache.pdfbox.pdmodel.PDDocument;
import javax.print.PrintService;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

/**
 * Created by rest on 1/27/15.
 */
public class PrintPdf {
    public static void main(String[] args) {
        printPDF("TestChineseFontType.pdf", choosePrinter());
    }

    public static PrintService choosePrinter() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        if(printJob.printDialog()) {
            return printJob.getPrintService();
        }
        else {
            return null;
        }
    }

    public static void printPDF(String fileName, PrintService printer){
        PrinterJob job = PrinterJob.getPrinterJob();
        try {
            job.setPrintService(printer);
        } catch (PrinterException e) {
            e.printStackTrace();
        }
        PDDocument doc = null;
        try {
            doc = PDDocument.load(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            doc.silentPrint(job);
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    public static void createPDF(){

    }
}
