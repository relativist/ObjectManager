package main;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rest on 1/29/15.
 */
public class DialogRepairWindow extends JDialog  {
    public JFrame frame;
    public JXDatePicker pickerBeginDate;
    public JXDatePicker pickerEndDate;
    public StringBuilder query;
    public JComboBox<String> comboState;

    public void addComponentsToPane(final Container paneMain) {

        query = new StringBuilder();

        paneMain.setLayout(new BorderLayout());

        final Container pane = new Container();
        pane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();

        Container rightPane = new Container();
        rightPane.setLayout(new BorderLayout());


        JLabel takerLabel = new JLabel("Статус объекта :");
        takerLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        ;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        pane.add(takerLabel, c);

        comboState = new JComboBox(new GeneralProjectClass().getComboBoxDataDB("OBJECTSTATE", "NAME").toArray());
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        pane.add(comboState, c);

        final JLabel lavelDateBegin = new JLabel("Дата начала: ");
        lavelDateBegin.setFont(new Font("Verdana", Font.PLAIN, 12));
        ;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        pane.add(lavelDateBegin, c);

        final JLabel lavelDateEnd = new JLabel("Дата конца: ");
        lavelDateEnd.setFont(new Font("Verdana", Font.PLAIN, 12));
        ;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 2;
        c.gridheight = 1;
        pane.add(lavelDateEnd, c);
        // Buttons

        ImageIcon icon = new ImageIcon("./icons/ok32.png");
        JButton buttonPrint = new JButton("Выбрать", icon);
        buttonPrint.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Click");


                long beginDate = pickerBeginDate.getDate().getTime();
                long endDate = pickerEndDate.getDate().getTime();
                if (beginDate > endDate) {
                    JOptionPane.showMessageDialog(frame, "Начальная дата больше конечной!.", "Inane warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String selectedState = comboState.getSelectedItem().toString();
//                System.out.println(selectedState);
                String selectedStateId = GeneralProjectClass.getComboBoxSingleDataDB("select id from objectstate where name=\"" + selectedState + "\";");
//                System.out.println(selectedStateId);

                String dateBegin = new SimpleDateFormat("yyyy-MM-dd").format(pickerBeginDate.getDate());
                String dateEnd = new SimpleDateFormat("yyyy-MM-dd").format(pickerEndDate.getDate());
                query.append("SELECT * FROM objecthistory WHERE stateid="+selectedStateId+" and date BETWEEN datetime('"+dateBegin+"') and datetime('"+dateEnd+"');");



                System.out.println("dialog repair window query = " + query);
//                JOptionPane.showMessageDialog(frame, "ok " + query, "Inane warning", JOptionPane.OK_OPTION);
                frame.dispose();

            }
        });
        c.weightx = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 1;
        pane.add(buttonPrint, c);

        icon = new ImageIcon("./icons/delete32.png");
        JButton buttonCansel = new JButton("Отменить", icon);
        buttonCansel.setHorizontalAlignment(SwingConstants.LEFT);
        buttonCansel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Click");
                frame.dispose();
            }
        });
        c.weightx = 1;
        c.gridx = 2;
        c.gridy = 1;
        c.gridheight = 1;
        pane.add(buttonCansel, c);

        c.weightx = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 1;
        paneMain.add(rightPane);

        pickerBeginDate = new JXDatePicker();
        pickerBeginDate.setDate(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        pickerBeginDate.setFormats(sdf);
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.fill = 1;
        pane.add(pickerBeginDate, c);

        pickerEndDate = new JXDatePicker();
        pickerEndDate.setDate(new Date());
        pickerEndDate.setFormats(sdf);
        pickerEndDate.setPreferredSize(new Dimension(145, 42));
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 1;
        pane.add(pickerEndDate, c);

        paneMain.add(pane);
    }


    public String createAndShowGUI()  {

        frame = new JFrame("Выберите интервал ремонтов оборудования");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JDialog dialog = new JDialog(frame);
        addComponentsToPane(dialog.getContentPane());
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setTitle("Выберите интервал ремонтов оборудования");
        dialog.setSize(new Dimension(400, 100));
        dialog.setLocationRelativeTo(frame);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);
        return query.toString();

    }
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        String s = new DialogRepairWindow().createAndShowGUI();
        System.out.println(s);
    }
}
