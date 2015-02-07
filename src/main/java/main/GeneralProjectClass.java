package main;


import org.jdesktop.swingx.table.DatePickerCellEditor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

public class GeneralProjectClass {
    public static String DBASE_PATH = "jdbc:sqlite:./base.db";
    public static final String VERSION = "0.56";
    public JButton buttonSearch;
    public JButton buttonAdd;
    public JButton buttonBack;
    public JButton buttonObject;
    public JButton buttonPrint;
    public JButton buttonRepair;
    public JButton buttonUser;
    public JButton buttonDelete;
    public JTable jtable;
    public JTextField textField;
    public JScrollPane jScrollPane;
    public JFrame frame;
    public String removableId;
    public Stack<Map<String, String>> historyMove;
    public List<String[]> convertableList = new ArrayList<>();

    public String convertTableCaption(String caption) {
        String result = caption;

        for(String [] entry: convertableList){
            if(caption.toUpperCase().equals(entry[0].toUpperCase())){
                result = entry[1];
                break;
            }else if (caption.toUpperCase().equals(entry[1].toUpperCase())){
                result = entry[0];
                break;
            }
        }

        if(result.equals(caption))
            System.out.println("unknown convertation "+caption);


        return result;
    }

    public void getDbasePath() throws FileNotFoundException {
        File file = new File("./");
        // Выяснянем, папка ли это.
        if(file.isDirectory()){
            System.out.println("Directory:");

            // Получаем все файлы и папки.
            String [] s = file.list();
            for (String value : s) {
                System.out.println(value);
            }
            System.out.println("---------");
            ArrayList list = new ArrayList();
            Scanner in = new Scanner(new File("path.txt"));
            while (in.hasNextLine())
                list.add(in.nextLine());
            String myDbFile = "./base.db";
            System.out.println(list.get(0)+": path");
            if ((new File(myDbFile)).exists()) {
                DBASE_PATH = "jdbc:sqlite:" + myDbFile;
                System.out.println("found local");
            } else {
                DBASE_PATH = "jdbc:sqlite:" + list.get(0);
                System.out.println("use: "+DBASE_PATH);
            }

        }
        else{
            System.out.print("Not a drectory");
        }
    }

    public DefaultTableModel buildTableModel(String query) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        Statement stmt = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(DBASE_PATH);
        connection.setAutoCommit(false);
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData metaData = rs.getMetaData();

        // names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(convertTableCaption(metaData.getColumnName(column)));
        }

        // data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        //            JOptionPane.showMessageDialog(null, jScrollPane);
        rs.close();
        stmt.close();
        connection.close();


        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                    int rowFirstIndex = e.getFirstRow();
                    int rowLastIndex = e.getLastRow();
                    DefaultTableModel model = (DefaultTableModel) e.getSource();
                    if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != -1 && jtable.getSelectedRow()!=-1 && !historyMove.peek().get("table").toUpperCase().equals("OBJECTHISTORY")) {
                        System.out.println("UPDATE CELL");
                        int updatedColIndex = e.getColumn();
                        String updateColmn = convertTableCaption(jtable.getColumnName(updatedColIndex));
                        String updatedValue = (String) model.getValueAt(rowFirstIndex, updatedColIndex);
                        System.out.println("column: " + updateColmn + " value: " + updatedValue);
                        String id = String.valueOf(model.getValueAt(rowFirstIndex, jtable.getColumn("ID").getModelIndex()));
                        String changedId = null;
                        switch (updateColmn){
                            case "ROOMID":
                                try {
                                    changedId = getComboBoxSingleDataDB("select id from room where name=\""+updatedValue+"\"");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                System.out.println("TO CHANGE: "+changedId);
                                updateDB(historyMove.peek().get("table"), updateColmn, changedId, id);
                                break;
                            case "DEPARTMENTID":

                                try {
                                    changedId = getComboBoxSingleDataDB("select id from department where name=\""+updatedValue+"\"");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                System.out.println("TO CHANGE: "+changedId);
                                updateDB(historyMove.peek().get("table"), updateColmn, changedId, id);
                                break;
                            case "STATEID":
                                changedId = getComboBoxSingleDataDB("select id from objectstate where name=\""+updatedValue+"\"");
                                String username = String.valueOf(jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("USERID")).getModelIndex()));
                                String objectName;
                                String userID = getComboBoxSingleDataDB("select id from user where fio=\"" + username + "\"");

                                System.out.println("TO CHANGE: "+changedId);
                                System.out.println("add to history about state.");
                                System.out.println("1 objid = "+id);
                                System.out.println("2 stateid = "+changedId);
                                System.out.println("3 userName = " + username);
                                System.out.println("3 userid = "+userID);
                                System.out.println("5 date = "+getLocalTime());
                                System.out.println("updatecolumn = "+updateColmn);
                                System.out.println("table = "+historyMove.peek().get("table"));

                                //find previous history line with this objectId.
                                String maxHistoryId = getComboBoxSingleDataDB("select max(id) from objecthistory where objectid="+id+";");
                                System.out.println("maxhistoryId = "+maxHistoryId);
                                if(Integer.valueOf(getComboBoxSingleDataDB("select count(*) from objecthistory where STATEID="+changedId+" and USERID=\""+userID+"\" and OBJECTID="+id+" and id="+maxHistoryId+";"))>0){
                                    System.out.println("skipp! Nothing changed!");
                                }
                                else {
                                    System.out.println("Refresh data!");
                                    insertHistoryDB(id,changedId,userID,"Состояние :"+updatedValue);
                                    updateDB(historyMove.peek().get("table"), updateColmn, changedId, id);
                                }
                                break;
                            case "USERID":
                                changedId = getComboBoxSingleDataDB("select id from user where fio=\""+updatedValue+"\"");
                                System.out.println("TO CHANGE: "+changedId);

                                System.out.println("add to history about user.");
                                System.out.println("1 objid = "+id);

                                String state = String.valueOf(jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("STATEID")).getModelIndex()));
                                objectName = String.valueOf(jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("NAME")).getModelIndex()));
                                System.out.println("3 userName = " + objectName);
                                String stateId=null;
                                stateId = getComboBoxSingleDataDB("select id from objectstate where name=\""+state+"\"");

                                System.out.println("2 stateid = "+stateId);
                                System.out.println("3 userid = "+changedId);

                                //find previous history line with this objectId.
                                maxHistoryId = getComboBoxSingleDataDB("select max(id) from objecthistory where objectid="+id+";");
                                System.out.println("maxhistoryId = "+maxHistoryId);
                                if(Integer.valueOf(getComboBoxSingleDataDB("select count(*) from objecthistory where STATEID="+stateId+" and USERID=\""+changedId+"\" and OBJECTID="+id+" and id="+maxHistoryId+";"))>0){
                                    System.out.println("skipp! Nothing changed!");
                                }
                                else {
                                    System.out.println("Refresh data!");
                                    insertHistoryDB(id,stateId,changedId,"Владелец: "+updatedValue);
                                    updateDB(historyMove.peek().get("table"), updateColmn, changedId, id);
                                }


                                break;
                            case "TYPEID":
                                try {
                                    changedId = getComboBoxSingleDataDB("select id from objecttype where name=\""+updatedValue+"\"");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                System.out.println("TO CHANGE: "+changedId);
                                updateDB(historyMove.peek().get("table"), updateColmn, changedId, id);
                                break;
                            case "NAME":
                            case "EAN":
                            case "FIO":
                                String testQuery = "select count(*) from "+historyMove.peek().get("table")+" where "+updateColmn+" = \""+updatedValue+"\" and id!="+id+";";
                                if(Integer.valueOf(getComboBoxSingleDataDB(testQuery))>0){
                                    JOptionPane.showMessageDialog(frame, "Значение НЕ ИЗМЕНИЛОСЬ!\nОно уже используется!\nСмените "+updateColmn+" = "+updatedValue+"\nОбновите таблицу!", "ОШИБКА!", JOptionPane.ERROR_MESSAGE);
                                }
                                else {
                                    updateDB(historyMove.peek().get("table"), updateColmn, updatedValue, id);
                                }
                                break;
                            default:
                                updateDB(historyMove.peek().get("table"), updateColmn, updatedValue, id);
                        }
                    } else if (e.getType() == TableModelEvent.INSERT && !historyMove.peek().get("table").toUpperCase().equals("OBJECTHISTORY")) {
                        System.out.println("INSERT ROW");
                        for (int i = rowFirstIndex; i <= rowLastIndex; i++) {
                            Vector rowData = (Vector) model.getDataVector().get(i);

                            Map<String, String> dataMap = new HashMap<String, String>();

                            for (int j = 0; j < rowData.size(); j++) {
                                if (convertTableCaption(jtable.getColumnName(j)).toUpperCase().equals("ID"))
                                    continue;
                                dataMap.put(convertTableCaption(jtable.getColumnName(j)), (String) rowData.get(j));
                                System.out.println(convertTableCaption(jtable.getColumnName(j)));
                                System.out.println((String) rowData.get(j));
                            }
                            try {
                                insertDB();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                        }
                    } else if (e.getType() == TableModelEvent.DELETE) {

                        System.out.println("DELETE ROW");
                        removeDB(historyMove.peek().get("table"));
                    } else if (e.getType() == TableModelEvent.UPDATE && e.getColumn() != -1 && jtable.getSelectedRow()!=-1 && historyMove.peek().get("table").toUpperCase().equals("OBJECTHISTORY")) {
                        System.out.println("EDIT OBJECT HISTORY!");
                        int updatedColIndex = e.getColumn();
                        String updateColumnName = convertTableCaption(jtable.getColumnName(updatedColIndex));
                        String updatedValue;
                        Object result = model.getValueAt(rowFirstIndex, updatedColIndex);
                        if(result.getClass() == Date.class) {
                            updatedValue = new SimpleDateFormat("yyyy-MM-dd").format(result);
                        }
                        else {
                            updatedValue = (String) result;
                        }
                        String id = String.valueOf(model.getValueAt(rowFirstIndex, jtable.getColumn("ID").getModelIndex()));
                        System.out.println("column: " + updateColumnName + " value: " + updatedValue+" id: "+id);

                        switch (updateColumnName.toUpperCase()){
                            case "DATE":
                                if (updatedValue.toString().length()>0)
                                updateDB("update objecthistory set date=datetime('"+updatedValue+"') where id="+id+";");
                                break;
                            case "DESCRIPTION":
                                updateDB("update objecthistory set description=\""+updatedValue+"\" where id="+id+";");
                                break;
                        }
                    }

                }
            });
            System.out.println("NEW TABLE MODEL CREATED");
        return model;

    }

    private void removeDB(String table) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String query = "DELETE FROM " + table + " WHERE ID=" + removableId + ";";
            System.out.println(query);
            stmt.executeUpdate(query);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private void updateDB(String table, String updateColmn, String updatedValue, String id) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String query = "UPDATE " + table + " SET " + updateColmn + "=\"" + updatedValue + "\" WHERE ID=" + id + ";";
            if(updateColmn.contains("ID"))
                query = "UPDATE " + table + " SET " + updateColmn + "=" + updatedValue + " WHERE ID=" + id + ";";
            System.out.println(query);
            stmt.executeUpdate(query);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void updateDB(String query) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            System.out.println(query);
            stmt.executeUpdate(query);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public List<String> getComboBoxDataDB(String table, String colmn) {
        Connection c = null;
        Statement stmt = null;
        List<String> result = new ArrayList<String>();
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String query = "Select "+colmn+" from "+table+";";
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result.add(rs.getString(colmn));
            }
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return result;
    }

    public static String getComboBoxSingleDataDB(String query) {
        Connection c = null;
        Statement stmt = null;
        String result = "";
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result = rs.getString(1);
            }
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("getComboBoxSingleDataDB: "+result+" "+query);
        return result;
    }

    private boolean isContainsDataDB(String query) {
        Connection c = null;
        Statement stmt = null;
        Boolean result = true;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            System.out.println("isContainsDB: "+query);
            ResultSet rs = stmt.executeQuery(query);
            if(rs.getFetchSize()==0) {
                result = false;
            }

            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("isContainsDB: "+result);
        return result;
    }

    private void insertDB()  {
        Connection c = null;
        Statement stmt = null;
        boolean isNeedPostInsert=false;
        String userId="1";
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String query = "";
            String changeMeCaption = "ИЗМЕНИ "+String.valueOf(System.currentTimeMillis());
            String table = historyMove.peek().get("table");
            switch (table.toUpperCase()) {
                case "ROOM":
                    String departmentId = historyMove.peek().get("query").replaceAll("\\D", "");
                    if(departmentId.length()<1)
                        departmentId="1";
                    updateDB("update "+table+" set name=\""+changeMeCaption+"\" where name=\"\";");
                    query = "INSERT INTO room (name,DESCRIPTION,DEPARTMENTID) VALUES (\"\",\"\","+departmentId+");";
                    stmt.executeUpdate(query);
                    break;
                case "DEPARTMENT":
//                    query = "INSERT INTO department (name,DESCRIPTION) VALUES (\""+tmp+"\",\"\");";
                    query = "INSERT INTO department (name,DESCRIPTION) VALUES (\"\",\"\");";
                    updateDB("update "+table+" set name=\""+changeMeCaption+"\" where name=\"\";");
                    stmt.executeUpdate(query);
                    break;
                case "USER":
                    String roomId = historyMove.peek().get("query").replaceAll("\\D", "");
                    if(roomId.length()<1)
                        roomId="1";
                    updateDB("update "+table+" set fio=\""+changeMeCaption+"\" where fio=\"\";");
                    query = "INSERT INTO user (fio,job,ROOMID) VALUES (\"\",\"\","+roomId+");";
                    stmt.executeUpdate(query);
                    break;
                case "OBJECT":
                    userId=historyMove.peek().get("query").replaceAll("\\D","");
                    if(userId.length()<1)
                        userId="1";
                    isNeedPostInsert=true;
                    query = "INSERT INTO object (EAN,COD,NAME,TYPEID,STATEID,USERID) VALUES (\""+String.valueOf(System.currentTimeMillis())+"\",\"\",\"\",1,1,"+userId+");";
                    stmt.executeUpdate(query);
                    break;
                case "OBJECTTYPE":
                    query = "INSERT INTO OBJECTTYPE (NAME,DESCRIPTION) VALUES (\"\",\"\");";
                    updateDB("update "+table+" set name=\""+changeMeCaption+"\" where name=\"\";");
                    stmt.executeUpdate(query);
                    break;
                case "OBJECTSTATE":
                    updateDB("update "+table+" set name=\""+changeMeCaption+"\" where name=\"\";");
                    query = "INSERT INTO OBJECTSTATE (NAME) VALUES (\"\");";
                    stmt.executeUpdate(query);
                    break;
                default:
                    System.out.println("unknown insertion!");
                    break;
            }
            System.out.println(query + " INSERTED");

            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Невозможно создать запись!", "ОШИБКА!", JOptionPane.ERROR_MESSAGE);
            //System.exit(0);
        }
        if (isNeedPostInsert) {
            String lastObjectId = getComboBoxSingleDataDB("select max(id) from object;");
            insertHistoryDB(lastObjectId,"1",userId,"Создан объект");
        }
    }

    private void insertHistoryDB(String objectId,String stateId, String userId,String description) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DBASE_PATH);
            c.setAutoCommit(true);
            stmt = c.createStatement();
            String query = "";
            query = "INSERT INTO objecthistory (OBJECTID,STATEID,USERID,DESCRIPTION) VALUES ("+objectId+","+stateId+","+userId+",\""+description+"\");";
            stmt.executeUpdate(query);
            System.out.println(query + " INSERTED");
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public void focusOnFirstJtableRow(){
        if(jtable.getRowCount()==0)
            return;
        jtable.requestFocus();
        jtable.setRowSelectionInterval(0,0);
        jtable.setColumnSelectionInterval(1,1);
    }

    public void focusOnLastJtableRow(){
        if(jtable.getRowCount()==0)
            return;
        jtable.requestFocus();
        jtable.setRowSelectionInterval(jtable.getRowCount()-1, jtable.getRowCount()-1);
        jtable.setColumnSelectionInterval(1,1);
    }

    public void addComponentsToPane(final Container paneMain) throws SQLException, ClassNotFoundException {

        paneMain.setLayout(new BorderLayout());


        final Container pane = new Container();
        pane.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();

        // LINE BROWSER
        System.out.println("MAIN PATH: ");
        StringBuilder resultPathString = new StringBuilder();
        for(Map<String,String> i : historyMove) {
            String nameItem = i.get("name");
            switch (i.get("table").toUpperCase()){
                case "USER":
                    if(i.get("query").toUpperCase().contains("WHERE"))
                        resultPathString.append(" > " + nameItem);
                    else
                        resultPathString.append(" > " + "Все Сотрудники");
                    break;
                case "OBJECT":
                    if(i.get("query").toUpperCase().contains("WHERE"))
                        resultPathString.append(" > " + nameItem);
                    else
                        resultPathString.append(" > " + "Все Оборудование");
                    break;
                default:
                    resultPathString.append(" > " + nameItem);
            }

        }
        JTextField textPath = new JTextField(resultPathString.toString().replaceAll("^ > ",""));
        textPath.setEditable(false);
        textPath.setEnabled(false);
        textPath.setDisabledTextColor(Color.BLACK);
//        textPath.setFont(new Font("Serif", Font.BOLD, 11));
//        textPath.setFont(new Font("Courier New", Font.BOLD, 11));
        textPath.setFont(new Font("TimesRoman", Font.BOLD, 11));
//        textPath.setFont(new Font("Verdana", Font.BOLD, 11));
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight=1;
        pane.add(textPath, c);

        JLabel emptyLabel = new JLabel();
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 1;
        pane.add(emptyLabel, c);

        textField = new JTextField("");
        textField.setFont(new Font("Verdana", Font.PLAIN, 20));
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if(e.getKeyCode()==KeyEvent.VK_ENTER){
                    System.out.println("Pressed enter for search text field!");
                    buttonSearch.doClick();
                    textField.requestFocus();
                    textField.setText("");
                }
                if (e.isControlDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_N:
                            System.out.println("HOT KEY PRESSED N = new row will add");
                            buttonAdd.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_LEFT:
                            System.out.println("HOT KEY PRESSED LEFT = go back by path");
                            buttonBack.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_BACK_SPACE:
                            System.out.println("HOT KEY PRESSED backspace = go back by path");
                            buttonBack.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_DELETE:
                            System.out.println("HOT KEY PRESSED delete = remove row");
                            buttonDelete.doClick();
                            focusOnFirstJtableRow();
                            break;
                    }
                }
            }
        });
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight=1;
        pane.add(textField, c);

        ImageIcon icon = new ImageIcon("./icons/search32.png");
        buttonSearch = new JButton("Поиск",icon);
        buttonSearch.setHorizontalAlignment(SwingConstants.LEFT);
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Поиск");
                String searchText = textField.getText();
                if (searchText.replaceAll(" ", "").length() < 1) {
                    textField.requestFocus();
                    return;
                }
                System.out.println("searching: " + searchText);
                System.out.println("search in EAN & CODE: " + searchText);
                if (Integer.valueOf(getComboBoxSingleDataDB("SELECT count(*) FROM object where ean='" + searchText + "' or cod='" + searchText + "';"))>0){
                    String query = "SELECT * FROM object where ean='" + searchText + "' or cod='" + searchText + "';";
                    System.out.println("found in ean and code:");
                    Map<String, String> user = new HashMap<>();
                    user.put("table", "object");
                    user.put("name", searchText);
                    user.put("query", query);
                    historyMove.push(user);
                    refreshForm(pane);
                    textField.setText("");
                } else if(Integer.valueOf(getComboBoxSingleDataDB( "SELECT count(*) FROM user where fio like '%" + searchText + "%';"))>0){
                    Map<String, String> user = new HashMap<>();
                    System.out.println("nothing found in EAN & CODE: " + searchText);
                    System.out.println("search in users FIOs: " + searchText);
                    String query =  "SELECT * FROM user where fio like '%" + searchText + "%';";
                    System.out.println(query);
                    user.put("table", "user");
                    user.put("name", searchText);
                    user.put("query",query);
                    historyMove.push(user);
                    refreshForm(pane);
                    textField.setText("");
                }else {
                    System.out.println("nothing found in users too!!!");
                    JOptionPane.showMessageDialog(pane, "Ни EAN, ни Code, ни FIO не найдены!");
                    textField.setText("");
                }
                textField.requestFocus();
            }

        });
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 0;
        c.gridheight=1;
        pane.add(buttonSearch, c);
        c.gridheight=1;

        icon = new ImageIcon("./icons/add32.png");
        buttonAdd = new JButton("Добавить",icon);
        buttonAdd.setHorizontalAlignment(SwingConstants.LEFT);
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("ADD");
                DefaultTableModel model = (DefaultTableModel) jtable.getModel();
                model.addRow(new Object[]{});
                try {
                    refreshForm(pane);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                focusOnLastJtableRow();
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 3;
        pane.add(buttonAdd, c);

        icon = new ImageIcon("./icons/delete32.png");
        buttonDelete = new JButton("Удалить",icon);
        buttonDelete.setHorizontalAlignment(SwingConstants.LEFT);
        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Delete");
                if (jtable.getSelectedRow() != -1) {

                    int reply = JOptionPane.showConfirmDialog(pane, "Уверен, что хочешь удалить? Если запись содержит внутренние обьекты,\nто доступ к ним будет невозможен!", "Удаление записи", JOptionPane.ERROR_MESSAGE);
                    if (reply == JOptionPane.YES_OPTION) {
                        removableId = String.valueOf(jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn("ID").getModelIndex()));
                        System.out.println("id to delete: " + removableId);
                        DefaultTableModel model = (DefaultTableModel) jtable.getModel();

                        model.removeRow(jtable.getSelectedRow());
                        try {
                            refreshForm(pane);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                focusOnFirstJtableRow();
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 4;
        pane.add(buttonDelete, c);

        icon = new ImageIcon("./icons/back32.png");
        buttonBack = new JButton("Назад",icon);
        buttonBack.setHorizontalAlignment(SwingConstants.LEFT);
        buttonBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Back");
                if (historyMove.size() > 1) {
                    historyMove.pop();
                    try {
                        refreshForm(pane);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                focusOnFirstJtableRow();
            }
        });
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 2;
        pane.add(buttonBack, c);

        icon = new ImageIcon("./icons/user32.png");
        buttonUser = new JButton("Пользователи",icon);
        buttonUser.setHorizontalAlignment(SwingConstants.LEFT);
        buttonUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("USERS");
                Map<String, String> user = new HashMap<>();
                user.put("table", "user");
                user.put("query", "SELECT * FROM user;");
                historyMove.push(user);
                try {
                    refreshForm(pane);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                focusOnFirstJtableRow();
            }
        });
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 6;
        pane.add(buttonUser, c);

        icon = new ImageIcon("./icons/object32.png");
        buttonObject = new JButton("Оборудование",icon);
        buttonObject.setHorizontalAlignment(SwingConstants.LEFT);
        buttonObject.setSize(200,200);
        buttonObject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("OBJECTS");
                Map<String, String> objects = new HashMap<>();
                objects.put("table", "object");
                objects.put("query", "SELECT * FROM object;");
                historyMove.push(objects);
                try {
                    refreshForm(pane);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                focusOnFirstJtableRow();
            }
        });
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 5;
        pane.add(buttonObject, c);

        icon = new ImageIcon("./icons/print32.png");
        buttonPrint = new JButton("Печать",icon);
        buttonPrint.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPrint.setSize(200,200);
        buttonPrint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("PRINT");
                if(historyMove.peek().get("table").toUpperCase().toString().equals("OBJECT")){
//                    if(historyMove.peek().get("table").toUpperCase().toString().equals("OBJECT") && jtable.getSelectedColumn()!=-1 && jtable.getSelectedRow()!=-1){
                    if(jtable.getSelectedColumn()!=-1 && jtable.getSelectedRow()!=-1){
                        System.out.println("object");
                        String ean= jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("EAN")).getModelIndex()).toString();
                        String cod= jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("COD")).getModelIndex()).toString();
                        String name= jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("NAME")).getModelIndex()).toString();
                        String typeId= jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn(convertTableCaption("TYPEID")).getModelIndex()).toString();
                        System.out.println(ean + " " + cod + " " + name);
                        try {
                            new BarCodeGenerator().printBarcode(ean,cod+" "+typeId+" "+name);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    else if(historyMove.peek().get("query").toUpperCase().contains("WHERE") && jtable.getSelectedColumn()==-1 && jtable.getSelectedRow()==-1 && jtable.getRowCount()>0){
                        System.out.println("user card print!");

                        List<Map<String, String>> objects = new ArrayList<>();
                        Map<String, String> map;
                        String user = "nobody";
                        for (int i = 0; i < jtable.getRowCount(); i++) {
                            String typeId = jtable.getValueAt(i, jtable.getColumn(convertTableCaption("TYPEID")).getModelIndex()).toString();
                            String name = jtable.getValueAt(i, jtable.getColumn(convertTableCaption("NAME")).getModelIndex()).toString();
                            String ean = jtable.getValueAt(i, jtable.getColumn(convertTableCaption("EAN")).getModelIndex()).toString();
                            String code = jtable.getValueAt(i, jtable.getColumn(convertTableCaption("COD")).getModelIndex()).toString();
                            user = jtable.getValueAt(i, jtable.getColumn(convertTableCaption("USERID")).getModelIndex()).toString();
                            map = new HashMap<>();
                            map.put("type",typeId);
                            map.put("name", name);
                            map.put("ean", ean);
                            map.put("code", code);
                            objects.add(map);
                        }
                        // To print
                        String roomName = getComboBoxSingleDataDB("select name from room where id = (select roomid from user where fio='"+user+"');");
                        String departmentName = getComboBoxSingleDataDB("select name from department where id = (select id from room where id = (select roomid from user where fio='"+user+"'));");
                        try {
                            new DialogUserWindow().createAndShowGUI(user,roomName, departmentName,objects);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                focusOnFirstJtableRow();
            }
        });
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 7;
        pane.add(buttonPrint, c);

        icon = new ImageIcon("./icons/repair32.png");
        buttonRepair = new JButton("Ремонт",icon);
        buttonRepair.setHorizontalAlignment(SwingConstants.LEFT);
        buttonRepair.setSize(200,200);
        buttonRepair.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("PRINT");
                String resultQuery = new DialogRepairWindow().createAndShowGUI();
                if(resultQuery.length()==0)
                    return;
                Map<String, String> history = new HashMap<>();
                history.put("table", "objecthistory");
                history.put("query",resultQuery);
                history.put("name","Ремонт");
                historyMove.push(history);
                refreshForm(pane);
                focusOnFirstJtableRow();
            }
        });
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 8;
        pane.add(buttonRepair, c);

        jtable = new JTable();
        jScrollPane = new JScrollPane(jtable);
        showTable(historyMove.peek().get("query"),pane);
//        jtable.setCellEditor();
//        DefaultCellEditor singleclick = new DefaultCellEditor(new JTextField());
//        singleclick.setClickCountToStart(0);
//        jtable.setCellEditor(singleclick);
//        jtable.setCellEditor(new MyTableCellEditor());

        jtable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.isControlDown()) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            System.out.println("HOT KEY PRESSED ENTER");
//                            buttonAdd.doClick();
                            try {
                                Robot robot = new Robot();
                                robot.mousePress(InputEvent.BUTTON3_MASK);
                                robot.mouseRelease(InputEvent.BUTTON3_MASK);
                            } catch (AWTException e1) {
                                e1.printStackTrace();
                            }
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_N:
                            System.out.println("HOT KEY PRESSED N = new row will add");
                            buttonAdd.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_LEFT:
                            System.out.println("HOT KEY PRESSED LEFT = go back by path");
                            buttonBack.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_BACK_SPACE:
                            System.out.println("HOT KEY PRESSED backspace = go back by path");
                            buttonBack.doClick();
                            focusOnFirstJtableRow();
                            break;
                        case KeyEvent.VK_DELETE:
                            System.out.println("HOT KEY PRESSED delete = remove row");
                            buttonDelete.doClick();
                            focusOnFirstJtableRow();
                            break;
                    }
                }
            }
        });
        // ПЕРЕХОДЫ ПО ПРАВОМУ ЩЕЛЧКУ МЫШИ!
        jtable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (SwingUtilities.isRightMouseButton(e) && jtable.getSelectedRow() != -1 && jtable.getSelectedColumn() != -1) {
                    System.out.println(jtable.getValueAt(jtable.getSelectedRow(), jtable.getSelectedColumn()).toString());
                    String id = jtable.getValueAt(jtable.getSelectedRow(), jtable.getColumn("ID").getModelIndex()).toString();
                    System.out.println("MAIN ID=" + id);
                    String columnName = convertTableCaption(jtable.getColumnName(jtable.getSelectedColumn()));
                    System.out.println("Column name : "+columnName);
                    switch (historyMove.peek().get("table").toUpperCase()){
                        case "ROOM":
                            Map<String, String> user = new HashMap<>();
                            user.put("name", getComboBoxSingleDataDB("select name from room where id=" + id + ";"));
                            user.put("table", "user");
                            user.put("query", "SELECT * FROM USER WHERE ROOMID="+id+";");
                            historyMove.push(user);
                            refreshForm(pane);
                            focusOnFirstJtableRow();
                            break;
                        case "DEPARTMENT":
                            Map<String, String> department = new HashMap<>();
                            department.put("name", getComboBoxSingleDataDB("select name from department where id=" + id + ";"));
                            department.put("table", "room");
                            department.put("query", "SELECT * FROM ROOM WHERE DEPARTMENTID="+id+";");
                            historyMove.push(department);
                            refreshForm(pane);
                            focusOnFirstJtableRow();
                            break;
                        case "USER":
                            Map<String, String> object = new HashMap<>();
                            object.put("table", "object");
                            object.put("name", getComboBoxSingleDataDB("select fio from USER where id=" + id + ";"));
                            object.put("query", "SELECT * FROM OBJECT WHERE USERID="+id+";");
                            historyMove.push(object);
                            refreshForm(pane);
                            focusOnFirstJtableRow();
                            break;
                        case "OBJECT":
                            switch (columnName) {
                                case "TYPEID":
                                    Map<String, String> type = new HashMap<>();
                                    type.put("table", "OBJECTTYPE");
                                    type.put("name", "Виды техники");
                                    type.put("query", "SELECT * FROM OBJECTTYPE;");
                                    historyMove.push(type);
                                    try {
                                        refreshForm(pane);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                    focusOnFirstJtableRow();
                                    break;
                                case "STATEID":
                                    Map<String, String> state = new HashMap<>();
                                    state.put("table", "OBJECTSTATE");
                                    state.put("name", "Состояния");
                                    state.put("query", "SELECT * FROM OBJECTSTATE;");
                                    historyMove.push(state);
                                    try {
                                        refreshForm(pane);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                    focusOnFirstJtableRow();
                                    break;
                                case "EAN":
                                    System.out.printf("go to object history. obj id = "+id);
                                    Map<String, String> history = new HashMap<>();
                                    history.put("table", "OBJECTHISTORY");
                                    history.put("name", getComboBoxSingleDataDB("select name from object where id=" + id + ";"));
                                    history.put("query", "SELECT * FROM OBJECTHISTORY where OBJECTID="+id+";");
                                    historyMove.push(history);
                                    try {
                                        refreshForm(pane);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                    focusOnFirstJtableRow();
                                    break;
                                default:
                                    System.out.println("unknown object go to");
                                    break;
                            }
                            break;
                        default:
                            System.out.println("unknown go to");
                            break;
                    }
                }
            }
        });
        jtable.setFont(new Font("Verdana", Font.PLAIN, 12));
        jtable.setColumnSelectionAllowed(true);

        //jtable
        c.fill = GridBagConstraints.BOTH;
        c.ipady = 200;
        c.ipadx = 200;
        c.weightx = 5.0;
        c.weighty = 5.0;
        c.gridwidth = 3;
        c.gridheight = 10;
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(5, 5, 5, 5);
        pane.add(jScrollPane, c);


        paneMain.add(pane);


    }

    private String getLocalTime(){
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    private void refreshForm(Container pane)  {
        pane.removeAll();
        try {
            addComponentsToPane(frame.getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void showTable(String query, final Container pane) throws SQLException, ClassNotFoundException {
        jtable = new JTable(buildTableModel(query));

        // create comboBox (комбобокс)
        if(jtable.getRowCount()>0) {
            String query2;
            String found;
            switch (historyMove.peek().get("table").toUpperCase()) {
                case "USER":
                    JComboBox comboBoxUser = new JComboBox(getComboBoxDataDB("ROOM", "NAME").toArray());
                    //right mouse on combobox
                    comboBoxUser.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            super.mouseReleased(e);
                            if (SwingUtilities.isRightMouseButton(e) && jtable.getSelectedRow() != -1 && jtable.getSelectedColumn() != -1) {
                                Map<String, String> type = new HashMap<String, String>();
                                type.put("table", "ROOM");
                                type.put("name", "Комнаты");
                                type.put("query", "SELECT * FROM ROOM;");
                                historyMove.push(type);
                                try {
                                    refreshForm(pane);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                focusOnFirstJtableRow();
                            }
                        }
                    });
                    for (int i = 0; i < jtable.getRowCount(); i++) {
                        query2 = "select name from room where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("ROOMID")).getModelIndex()) + ";";
                        found = getComboBoxSingleDataDB(query2);
                        System.out.println("found : " + found);
                        jtable.setValueAt(found, i, jtable.getColumn(convertTableCaption("ROOMID")).getModelIndex());
                    }
                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("ROOMID")).getModelIndex()).setCellEditor(new DefaultCellEditor(comboBoxUser));
                    break;
                case "ROOM":
                    JComboBox comboBoxRoom = new JComboBox(getComboBoxDataDB("DEPARTMENT", "NAME").toArray());
                    //right mouse on combobox
                    comboBoxRoom.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            super.mouseReleased(e);
                            if (SwingUtilities.isRightMouseButton(e) && jtable.getSelectedRow() != -1 && jtable.getSelectedColumn() != -1) {
                                Map<String, String> type = new HashMap<String, String>();
                                type.put("table", "DEPARTMENT");
                                type.put("name", "Отделы");
                                type.put("query", "SELECT * FROM DEPARTMENT;");
                                historyMove.push(type);
                                try {
                                    refreshForm(pane);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                focusOnFirstJtableRow();
                            }
                        }
                    });
                    query2 = "select name from DEPARTMENT where id=" + jtable.getValueAt(0, jtable.getColumn(convertTableCaption("DEPARTMENTID")).getModelIndex()) + ";";
                    found = getComboBoxSingleDataDB(query2);
                    System.out.println("found : " + found);
                    for (int i = 0; i < jtable.getRowCount(); i++) {
                        jtable.setValueAt(found, i, jtable.getColumn(convertTableCaption("DEPARTMENTID")).getModelIndex());
                    }
                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("DEPARTMENTID")).getModelIndex()).setCellEditor(new DefaultCellEditor(comboBoxRoom));
                    break;
                case "OBJECT":
                    JComboBox comboBoxType = new JComboBox(getComboBoxDataDB("OBJECTTYPE", "NAME").toArray());

                    //right mouse on combobox
                    comboBoxType.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            super.mouseReleased(e);
                            if (SwingUtilities.isRightMouseButton(e) && jtable.getSelectedRow() != -1 && jtable.getSelectedColumn() != -1) {
                                Map<String, String> type = new HashMap<String, String>();
                                type.put("table", "OBJECTTYPE");
                                type.put("name", "Виды техники");
                                type.put("query", "SELECT * FROM OBJECTTYPE;");
                                historyMove.push(type);
                                try {
                                    refreshForm(pane);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                focusOnFirstJtableRow();
                            }
                        }
                    });
                    JComboBox comboBoxState = new JComboBox(getComboBoxDataDB("OBJECTSTATE", "NAME").toArray());

                    comboBoxState.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            super.mouseReleased(e);
                            if (SwingUtilities.isRightMouseButton(e) && jtable.getSelectedRow() != -1 && jtable.getSelectedColumn() != -1) {
                                Map<String, String> state = new HashMap<String, String>();
                                state.put("table", "OBJECTSTATE");
                                state.put("name", "Состояния");
                                state.put("query", "SELECT * FROM OBJECTSTATE;");
                                historyMove.push(state);
                                try {
                                    refreshForm(pane);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                focusOnFirstJtableRow();
                            }
                        }
                    });

                    comboBoxUser = new JComboBox(getComboBoxDataDB("USER", "FIO").toArray());

                    for (int i = 0; i < jtable.getRowCount(); i++) {
                        query2 = "select name from OBJECTTYPE where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("TYPEID")).getModelIndex()) + ";";
                        String queryState = "select name from OBJECTSTATE where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("STATEID")).getModelIndex()) + ";";
                        String queryUser = "select FIO from USER where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("USERID")).getModelIndex()) + ";";
                        found = getComboBoxSingleDataDB(query2);
                        String foundState = getComboBoxSingleDataDB(queryState);
                        String foundUser = getComboBoxSingleDataDB(queryUser);
                        jtable.setValueAt(found, i, jtable.getColumn(convertTableCaption("TYPEID")).getModelIndex());
                        jtable.setValueAt(foundState, i, jtable.getColumn(convertTableCaption("STATEID")).getModelIndex());
                        jtable.setValueAt(foundUser, i, jtable.getColumn(convertTableCaption("USERID")).getModelIndex());
                    }

                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("TYPEID")).getModelIndex()).setCellEditor(new DefaultCellEditor(comboBoxType));
                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("STATEID")).getModelIndex()).setCellEditor(new DefaultCellEditor(comboBoxState));
                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("USERID")).getModelIndex()).setCellEditor(new DefaultCellEditor(comboBoxUser));
                    break;

                case "OBJECTHISTORY":
                    int rowHeight = jtable.getRowHeight();
                    for (int i = 0; i < jtable.getRowCount(); i++) {
                        String queryObject = "select name from OBJECT where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("OBJECTID")).getModelIndex()) + ";";
                        String queryState = "select name from OBJECTSTATE where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("STATEID")).getModelIndex()) + ";";
                        String queryUser = "select FIO from USER where id=" + jtable.getValueAt(i, jtable.getColumn(convertTableCaption("USERID")).getModelIndex()) + ";";
                        String foundObject = getComboBoxSingleDataDB(queryObject);
                        String foundState = getComboBoxSingleDataDB(queryState);
                        String foundUser = getComboBoxSingleDataDB(queryUser);
                        System.out.println("found : " + foundObject);
                        System.out.println("found : " + foundState);
                        System.out.println("found : " + foundUser);
                        jtable.setValueAt(foundObject, i, jtable.getColumn(convertTableCaption("OBJECTID")).getModelIndex());
                        jtable.setValueAt(foundState, i, jtable.getColumn(convertTableCaption("STATEID")).getModelIndex());
                        jtable.setValueAt(foundUser, i, jtable.getColumn(convertTableCaption("USERID")).getModelIndex());
                    }

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    DatePickerCellEditor datePickerCellEditor = new DatePickerCellEditor(format);
                    datePickerCellEditor.setClickCountToStart(-1);
                    datePickerCellEditor.setFormats(format);
                    jtable.getColumnModel().getColumn(jtable.getColumn(convertTableCaption("DATE")).getModelIndex()).setCellEditor(datePickerCellEditor);
                    break;
                default:
                    System.out.println("unknown replacement comboBox");
            }
        }
        hideJtableID();
        jScrollPane = new JScrollPane(jtable);

    }

    private void hideJtableID() {
        jtable.getColumn("ID").setMaxWidth(0);
        jtable.getColumn("ID").setMinWidth(0);
        jtable.getColumn("ID").setWidth(0);
        jtable.getColumn("ID").setPreferredWidth(0);
    }

    private void createAndShowGUI() throws SQLException, ClassNotFoundException, FileNotFoundException {

        convertableList.add(new String[]{"NAME", "Наименование"});
        convertableList.add(new String[]{"EAN", "Штрих-Код"});
        convertableList.add(new String[]{"DESCRIPTION", "Описание"});
        convertableList.add(new String[]{"COD", "Бух-код"});
        convertableList.add(new String[]{"TYPEID", "Тип"});
        convertableList.add(new String[]{"STATEID", "Состояние"});
        convertableList.add(new String[]{"USERID", "Владелец"});
        convertableList.add(new String[]{"OBJECTID", "Объект"});
        convertableList.add(new String[]{"DATE", "Дата"});
        convertableList.add(new String[]{"DEPARTMENTID", "Отдел"});
        convertableList.add(new String[]{"FIO", "Ф.И.О."});
        convertableList.add(new String[]{"JOB", "Должность"});
        convertableList.add(new String[]{"PHONE", "Телефон"});
        convertableList.add(new String[]{"NETNAME", "Сетевое имя"});
        convertableList.add(new String[]{"ROOMID", "Кабинет"});

        //find database near the project.
        getDbasePath();

        frame = new JFrame("Object Manager 2015 v"+VERSION);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        historyMove = new Stack<Map<String, String>>();
        Map<String, String> department = new HashMap<>();
        department.put("query", "SELECT * FROM department;");
        department.put("table", "department");
        department.put("name", "Путь:");
        historyMove.push(department);

        addComponentsToPane(frame.getContentPane());
        frame.setIconImage(new ImageIcon("./icons/logo.png").getImage());

        frame.setSize(1000, 500);
        frame.setLocation(300, 300);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                            new GeneralProjectClass().createAndShowGUI();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

            }
        });
//        new GeneralProjectClass().createAndShowGUI();
    }
}