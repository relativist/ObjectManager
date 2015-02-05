package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.output.bitmap.BitmapEncoder;
import org.krysalis.barcode4j.output.bitmap.BitmapEncoderRegistry;
import org.krysalis.barcode4j.tools.UnitConv;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

public class BarCodeGenerator {

    public void printBarcode(String code, String message) throws IOException {
        String[] paramArr = splitIt(message, 45);
        //new String[] {"Barcode4J Some long information","another information"};
        Code128Bean bean = new Code128Bean();
        final int dpi = 100;
        bean.setModuleWidth(UnitConv.in2mm(1.8f / dpi));
        bean.setBarHeight(UnitConv.in2mm(50f / dpi));
        bean.doQuietZone(false);
        bean.setFontSize(4f);
        boolean antiAlias = false;
        int orientation = 0;
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                dpi, BufferedImage.TYPE_BYTE_BINARY, antiAlias, orientation);
        bean.generateBarcode(canvas, code);
        canvas.finish();
        BufferedImage symbol = canvas.getBufferedImage();
        int fontSize = 10;
        int lineHeight = (int) (fontSize * 1.5);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        int width = symbol.getWidth();
        int height = symbol.getHeight();
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), antiAlias, true);
        for (int i = 0; i < paramArr.length; i++) {
            String line = paramArr[i];
            Rectangle2D bounds = font.getStringBounds(line, frc);
            width = (int) Math.ceil(Math.max(width, bounds.getWidth()));
            height += lineHeight;
        }
        int padding = 2;
        width += 2 * padding;
        height += 3 * padding;

        BufferedImage bitmap = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = (Graphics2D) bitmap.getGraphics();
        g2d.setBackground(Color.white);
        g2d.setColor(Color.black);
        g2d.clearRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        g2d.setFont(font);

        AffineTransform symbolPlacement = new AffineTransform();
        symbolPlacement.translate(padding, padding);
        g2d.drawRenderedImage(symbol, symbolPlacement);

        int y = padding + symbol.getHeight() + padding;
        for (int i = 0; i < paramArr.length; i++) {
            String line = paramArr[i];
            y += lineHeight;
            g2d.drawString(line, padding, y);
        }
        g2d.dispose();

        String mime = "image/png";
        OutputStream out = new FileOutputStream("./eans/"+code + ".png");
        try {
            final BitmapEncoder encoder = BitmapEncoderRegistry.getInstance(mime);
            encoder.encode(bitmap, out, mime, dpi);
        } finally {
            out.close();
        }
        printImage("./eans/"+code + ".png");
    }

    public String[] splitIt(String msg,int size){
        String[] splitLine =  msg.split(" ");
        StringBuilder builder = new StringBuilder();
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i <splitLine.length ; i++) {
            if (builder.toString().length()+splitLine[i].length()>size) {
                resultList.add(builder.toString());
                builder.delete(0, builder.toString().length());
            }
            builder.append(splitLine[i]);
            builder.append(" ");

            if(i==splitLine.length-1){
                resultList.add(builder.toString());
            }
        }
        String[] finalResult = new String[resultList.size()];
        finalResult = resultList.toArray(finalResult);
        return finalResult;
    }


    public void printImage(String path) throws IOException {
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(1));
        PrintService[] services= PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
        PrintRequestAttributeSet attributes=new HashPrintRequestAttributeSet();
        if(services.length==0){
            System.out.println("no printers found!");
            return;
        }
        PrintService ps = ServiceUI.printDialog(null, 50, 50, services, services[0], null, attributes);

        if(ps == null)
            return;

        System.out.println("Printing to " + ps);
        DocPrintJob job = ps.createPrintJob();
        FileInputStream fin = new FileInputStream(path);
        Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.GIF, null);
        try {
            job.print(doc, pras);
        } catch (PrintException e) {
            e.printStackTrace();
        }
        fin.close();
    }

    public static void main(String[] args) {
        try {
             new BarCodeGenerator().printBarcode("1234567890155","88584854654 Some ");
//             new BarCodeGenerator().printBarcode("1234567890123","88584854654 Some long message around it you will split it now! Please note this will moved to another line of bar code! look!");
//             new BarCodeGenerator().printBarcode(String.valueOf(System.currentTimeMillis()),"88584854654 Some long message around it you will split it now! Please note this will moved to another line of bar code! look!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}