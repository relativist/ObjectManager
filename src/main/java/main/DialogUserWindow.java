package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * Created by rest on 1/29/15.
 */
public class DialogUserWindow extends JDialog  {
    public JFrame frame;

    public void addComponentsToPane(final Container paneMain,final  String user,final String room,final String department, final List<Map<String,String>> objects) throws SQLException, ClassNotFoundException {

        paneMain.setLayout(new BorderLayout());

        final Container pane = new Container();
        pane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();

        JLabel takerLabel = new JLabel("Принимающий:");
        takerLabel.setFont(new Font("Verdana", Font.PLAIN, 18));;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight=1;
        pane.add(takerLabel, c);

        final JComboBox<String> giveTo = new JComboBox(new GeneralProjectClass().getComboBoxDataDB("USER", "FIO").toArray());
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight=1;
        pane.add(giveTo, c);

        final JComboBox<String> giveToObj = new JComboBox(new GeneralProjectClass().getComboBoxDataDB("USER", "FIO").toArray());
        c.weightx = 2;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight=1;
        pane.add(giveToObj, c);

        final JCheckBox checkBox = new JCheckBox("Переназначать объекты",true);
        checkBox.setFont(new Font("Verdana", Font.PLAIN, 12));;
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println(e.getStateChange() == ItemEvent.SELECTED ? "SELECTED" : "DESELECTED");
                if(e.getStateChange() == ItemEvent.DESELECTED){
                    giveToObj.setEnabled(false);
                }
                else {
                    giveToObj.setEnabled(true);
                }
            }
        });
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight=1;
        pane.add(checkBox, c);


        // Buttons

        ImageIcon icon = new ImageIcon("./icons/print32.png");
        JButton buttonPrint = new JButton("Печать",icon);
        buttonPrint.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Click");
                String giveToUserObjFio = giveToObj.getSelectedItem().toString();
                String giveToUserFio = giveTo.getSelectedItem().toString();
                if(checkBox.isSelected()){
                    System.out.println("selected!!! switch objects!");
                    for(Map<String,String>entry: objects) {
                        String query = "update object set userid=(select id from user where fio='" + giveToUserObjFio + "') where ean='"+entry.get("ean")+"';";
                        GeneralProjectClass.updateDB(query);
                    }

                }
                new CreateDocxDocument().createAndPrintUserCard(user, giveToUserFio,room, department,objects);
                frame.dispose();

            }
        });
        c.weightx = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight=1;
        pane.add(buttonPrint, c);

        icon = new ImageIcon("./icons/delete32.png");
        JButton buttonCansel = new JButton("Отменить",icon);
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
        c.gridheight=1;
        pane.add(buttonCansel, c);
        paneMain.add(pane);
    }

//    public void actionPerformed(ActionEvent e) {
//        frame.dispose();
//    }

    public void createAndShowGUI(String user,String room,String department,List<Map<String,String>> objects) throws SQLException, ClassNotFoundException, FileNotFoundException {

        frame = new JFrame("Dialog Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        addComponentsToPane(frame.getContentPane(),user,room,department,objects);
//        frame.setIconImage(new ImageIcon("./icons/logo.png").getImage());
//        frame.pack();
//        frame.setVisible(true);

        JDialog dialog = new JDialog(frame);
        addComponentsToPane(dialog.getContentPane(),user,room,department,objects);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setTitle("Dialog Window");
        dialog.setSize(new Dimension(400, 100));
        dialog.setLocationRelativeTo(frame);
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);
    }
    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
//        new DialogUserWindow().createAndShowGUI();
    }
}
