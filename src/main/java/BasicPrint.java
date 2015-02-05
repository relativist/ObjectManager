import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.Graphics2D;
import java.io.*;

import javax.swing.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class BasicPrint {
    JFrame frame;
    JButton btn;
    private boolean PrintJobDone = false;

    protected void MakeGui() {

        frame = new JFrame("PrintService");

        btn = new JButton("Cancel Print Job");
        btn.disable();
        frame.getContentPane().add(btn, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    BasicPrint(String FileToPrint, String pMode) {

        try {

            MakeGui();

            File baseDir = new File("./");
            File outDir = new File(baseDir, FileToPrint);

            // Open the image file
            InputStream is = new BufferedInputStream(new FileInputStream(
                    outDir));

            // Find the default service
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

            if (pMode != null && pMode.equalsIgnoreCase("TXT"))
                //        flavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF8;
                flavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16LE;
            else if (pMode != null && pMode.equalsIgnoreCase("PS"))
                flavor = DocFlavor.INPUT_STREAM.POSTSCRIPT;
            else if (pMode != null && pMode.equalsIgnoreCase("PDF"))
                flavor = DocFlavor.INPUT_STREAM.PDF;
            else if (pMode != null && pMode.equalsIgnoreCase("JPG"))
                flavor = DocFlavor.INPUT_STREAM.JPEG;
            else if (pMode != null && pMode.equalsIgnoreCase("GIF"))
                flavor = DocFlavor.INPUT_STREAM.GIF;
            else if (pMode != null && pMode.equalsIgnoreCase("PNG"))
                flavor = DocFlavor.INPUT_STREAM.PNG;
            else if (pMode != null && pMode.equalsIgnoreCase("PCL"))
                flavor = DocFlavor.INPUT_STREAM.PCL;
            else if (pMode != null && pMode.equalsIgnoreCase("RAW"))
                flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

            System.err.println("* IMPRIMIR " + FileToPrint + " " + pMode + " "
                    + flavor);

            PrintService dservice = PrintServiceLookup
                    .lookupDefaultPrintService();

            PrintService[] services = PrintServiceLookup.lookupPrintServices(
                    flavor, null);

            if (services == null || services.length < 1)
                services = PrintServiceLookup.lookupPrintServices(null, null);

            PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
            aset.add(new Copies(1));
            aset.add(OrientationRequested.PORTRAIT);
            // aset.add(MediaTray.MAIN);
            aset.add(Sides.ONE_SIDED);
            aset.add(MediaSizeName.ISO_A4);
            PrintService service = ServiceUI.printDialog(
                    (GraphicsConfiguration) null, 60, 60, services,
                    (PrintService) dservice, (DocFlavor) flavor, aset);

            if (service != null) {

                // Create the print job
                final DocPrintJob job = service.createPrintJob();

                Doc doc = new SimpleDoc(is, flavor, null);

                // Monitor print job events; for the implementation of
                // PrintJobWatcher,

                PrintJobWatcher pjDone = new PrintJobWatcher(job);

                if (job instanceof CancelablePrintJob) {

                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            CancelablePrintJob cancelJob = (CancelablePrintJob) job;
                            try {
                                cancelJob.cancel();
                            } catch (PrintException e) {
                                // Possible reason is job was already finished
                            }
                        }
                    });

                    btn.enable();
                }

                try {

                    // Print it

                    job.print(doc, (PrintRequestAttributeSet) aset);

                } catch (PrintException e) {
                    e.printStackTrace();
                }

                System.err.println("* Impresion Realizada - Esperando ..");
                // Wait for the print job to be done
                pjDone.waitForDone();

            }

            // It is now safe to close the input stream
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {

                synchronized (BasicPrint.this) {
                    PrintJobDone = true;
                    BasicPrint.this.notify();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void waitForDone() {
        try {
            while (!PrintJobDone) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            //args[0]="t";
            //args[1]="rr";
//            if (args.length < 1) {
//                System.err.println("\nSintaxis:\n\n java BasicPrint FileToPrint [pMode]\n");
//                System.exit(0);
//            }

            BasicPrint bp = null;

            //if (args.length < 2){
            //bp = new BasicPrint(args[0], null);


            //}else{
            //bp = new BasicPrint(args[0], args[1]);
            bp = new BasicPrint("report.docx","RAW");
            //}    
            bp.waitForDone();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PrintJobWatcher {

        // true iff it is safe to close the print job's input stream
        boolean done = false;

        int lastEvent = 0;

        PrintJobWatcher(DocPrintJob job) {

            // Add a listener to the print job
            job.addPrintJobListener(new PrintJobAdapter() {

                public void printJobRequiresAttention(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err
                            .println("* La impresora requiere de su Atencion ! * "
                                    + pje);
                    // allDone();
                }

                public void printDataTransferCompleted(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err
                            .println("* Transferencia de datos a la impresora OK. * "
                                    + pje);
                    // allDone();
                }

                public void printJobCanceled(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err.println("* Trabajo de impresion CANCELADO ! * "
                            + pje);
                    allDone();
                }

                public void printJobCompleted(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err.println("* Impresion completa OK. * " + pje);
                    allDone();
                }

                public void printJobFailed(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err.println("* ERROR en la Impresion ! * " + pje);
                    // allDone();
                }

                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    lastEvent = pje.getPrintEventType();
                    System.err
                            .println("* No mas eventos de impresion * " + pje);
                    allDone();
                }

                void allDone() {

                    synchronized (PrintJobWatcher.this) {
                        done = true;
                        PrintJobWatcher.this.notify();
                    }
                }
            });
        }

        /** Description of the Method */
        public synchronized void waitForDone() {
            try {
                while (!done) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}