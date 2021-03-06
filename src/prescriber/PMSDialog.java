/*Copyright 2008 by Vladimir Polony, Stupy 24, Banska Bystrica, Slovakia

This file is part of OpenRep FREE homeopathic software.

    OpenRep FREE is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenRep FREE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenRep FREE.  If not, see <http://www.gnu.org/licenses/>.*/

/* The main dialog of the Patient Management System
 * 
 *
 * Created on September 1, 2008, 9:48 AM
 */

package prescriber;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  vladimir
 */


public class PMSDialog extends javax.swing.JDialog {

    private final int SEARCH_ANYWHERE = 0;
    private final int SEARCH_ID = 1;
    private final int SEARCH_SURNAME = 2;
    private final int SEARCH_NAME = 3;
    private final int SEARCH_EMAIL = 4;
    private final int SEARCH_TELEPHONE = 5;
    private final int SEARCH_ADDRESS = 6;
    private final int SEARCH_COMMENT = 7;
    
    /** contains the list of all patients */
    private ArrayList<Patient> patients = new ArrayList();
    /** contains the name of the file where are the patient records saved */
    private String patient_filename;
    /** contains the parent of this dialog */
    java.awt.Frame parent;
    /** id of the currently selected patient */
    private int selected_patient_id = -1;
    /** contains the path to the pms files*/
    private String pms_path;
    /** a pointer to the main form of the openrep*/
    private PrescriberView open_rep_form;
    /** sort the patients table by the following column*/
    private int sort_by_col = 0;
    /** sort the patients table asc / desc*/
    private boolean sort_asc = true;
    /** sort the patients table asc / desc*/
    private int max_id_len = 1;
    /** contains the instance of the patient diagnosis dialog */
    private PatientDiagnosisDialog diagnosis_dialog = null;
    
    /** This is the filefilter that is used to display correct file extension types in the opendialogs
     * 
     */
    javax.swing.filechooser.FileFilter ZipFileFilter = new javax.swing.filechooser.FileFilter() {

        public boolean accept(File pathname) {
            if (pathname.isDirectory() || prescriber.Utils.ExtractFileExtension(pathname.getPath()).equalsIgnoreCase("zip")) {
                return true;
            }
            else
            return false;
        }

        @Override
        public String getDescription() {
            return "ZIP archive";
        }
    };
    
    /** This class is used to refresh the RemSymptomTable
     * 
     */
    class GeneratePatientTableThread {
        
        private boolean read_data = false;
        
        private int search_in;
        
        private String search_string;
        
        private ArrayList<Patient> SearchPatients (ArrayList<Patient> pat) {
            ArrayList<Patient> result = new ArrayList();
            for (int x = 0; x < pat.size(); x++) {
                if (search_in == SEARCH_ANYWHERE) {
                    if (pat.get(x).additional_information.toLowerCase().indexOf(search_string) != -1 ||
                        pat.get(x).address.toLowerCase().indexOf(search_string) != -1 ||
                        pat.get(x).comment.toLowerCase().indexOf(search_string) != -1 ||
                        pat.get(x).email.toLowerCase().indexOf(search_string) != -1 ||
                        Utils.FillChars(String.valueOf(pat.get(x).id), max_id_len, '0', true).indexOf(search_string) != -1 ||
                        pat.get(x).name.toLowerCase().indexOf(search_string) != -1 ||
                        pat.get(x).surname.toLowerCase().indexOf(search_string) != -1 ||
                        pat.get(x).telephone.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_ADDRESS) {
                    if (pat.get(x).address.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_COMMENT) {
                    if (pat.get(x).comment.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_EMAIL) {
                    if (pat.get(x).email.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_ID) {
                    if (Utils.FillChars(String.valueOf(pat.get(x).id), max_id_len, '0', true).indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_NAME) {
                    if (pat.get(x).name.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_SURNAME) {
                    if (pat.get(x).surname.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
                else
                if (search_in == SEARCH_TELEPHONE) {
                    if (pat.get(x).telephone.toLowerCase().indexOf(search_string) != -1) result.add(pat.get(x));
                }
            }
            return result;
        }
        
        private ArrayList<Patient> SortPatients (int sort_col, boolean asc, ArrayList<Patient> pat) {
            ArrayList<Patient> result = new ArrayList();
            ArrayList<String> sort_criteria = new ArrayList();
            for (int x = 0; x < pat.size(); x++) {
                if (sort_col == 0) sort_criteria.add(Utils.FillChars(String.valueOf(pat.get(x).id), max_id_len, '0', true));
                else
                if (sort_col == 1) sort_criteria.add(pat.get(x).surname);
                else
                if (sort_col == 2) sort_criteria.add(pat.get(x).name);
                else
                if (sort_col == 3) sort_criteria.add(pat.get(x).email);
                else
                if (sort_col == 4) sort_criteria.add(pat.get(x).telephone);
                else
                if (sort_col == 5) sort_criteria.add(pat.get(x).address);                    
                else
                if (sort_col == 6) sort_criteria.add(String.valueOf(pat.get(x).sex));
                else
                sort_criteria.add(String.valueOf(pat.get(x).date_of_birth));
            }
            Collections.sort(sort_criteria);            
                int start;
                int end;                
                start = 0;
                end = sort_criteria.size() - 1;
                int x = start;
                if (!asc) x = end;
                while (true) {
                    for (int y = 0; y < pat.size(); y++) {
                        if (sort_col == 0 && Utils.FillChars(String.valueOf(pat.get(y).id), max_id_len, '0', true).equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 1 && pat.get(y).surname.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 2 && pat.get(y).name.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 3 && pat.get(y).email.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 4 && pat.get(y).telephone.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }                            
                        else
                        if (sort_col == 5 && pat.get(y).address.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 6 && String.valueOf(pat.get(y).sex).equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 7 && pat.get(y).date_of_birth.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                    }
                    if (asc) x++;
                    else
                    x--;
                    if (x > (end+1) || x < start) break;
                
            }
            return result;
        }
        
        public void run() {
            if (read_data) ReadPatientFile(patient_filename);
/*            String[] columnNames = {
                        "ID",
                        "Surname",
                        "Name",
                        "E-mail",
                        "Telephone",                        
                        "Address",
                        "Sex",
                        "Age"};*/

            String[] columnNames = {
                        "ID",
                        "Name",
                        "Surname",
                        "Sex",
                        "Age",
                        "Address",
                        "Telephone",
                        "E-mail"};

            ArrayList<Patient> searched_patients = new ArrayList();
            if (search_in != -1 && search_string != null) searched_patients = SearchPatients(patients);
            else {
                for (int x = 0; x < patients.size(); x++) {
                    Patient p = new Patient();
                    p.DeepCopy(patients.get(x));
                    searched_patients.add(p);
                }
                //searched_patients = patients;
            }
            searched_patients = SortPatients(sort_by_col, sort_asc, searched_patients);
            String[][] data = new String[searched_patients.size()][8];
            int max_len = 1;
            for (int x = 0; x < searched_patients.size(); x++) {
                if (searched_patients.get(x).id > max_len) max_len = searched_patients.get(x).id;
            }
            max_len = String.valueOf(max_len).length();
            max_id_len = max_len;
            for (int y = 0; y < searched_patients.size(); y++) {
                data[y][0] = Utils.FillChars(String.valueOf(searched_patients.get(y).id), max_len, '0', true);
                data[y][1] = searched_patients.get(y).name;
                data[y][2] = searched_patients.get(y).surname;
                if (searched_patients.get(y).sex) data[y][3] = "M";
                else
                data[y][3] = "F";
                data[y][4] = Utils.ConvertDateToAge(searched_patients.get(y).date_of_birth);
                data[y][5] = searched_patients.get(y).address;
                data[y][6] = searched_patients.get(y).telephone;
                data[y][7] = searched_patients.get(y).email;
                
            }                                
            MyModel temp_t = new MyModel(data, columnNames);
            PatientTable.setModel(temp_t);
            PatientTable.setBackground(Color.WHITE);
            PatientTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            try {
                PatientTable.getColumnModel().getColumn(0).setPreferredWidth(50);
                PatientTable.getColumnModel().getColumn(1).setPreferredWidth(120);
                PatientTable.getColumnModel().getColumn(2).setPreferredWidth(120);
                PatientTable.getColumnModel().getColumn(3).setPreferredWidth(50);
                PatientTable.getColumnModel().getColumn(4).setPreferredWidth(50);
                PatientTable.getColumnModel().getColumn(5).setPreferredWidth(220);
                PatientTable.getColumnModel().getColumn(6).setPreferredWidth(100);
                PatientTable.getColumnModel().getColumn(7).setPreferredWidth(100);
                PatientTable.revalidate();
                PatientTable.repaint();
            } catch (Exception e) {}
    }

        public GeneratePatientTableThread() {
            this.read_data = false;
        }        
        
        public GeneratePatientTableThread(boolean read_data, int search_in, String search_string) {            
            this.read_data = read_data;
            this.search_in = search_in;
            this.search_string = search_string;
            if (this.search_string != null) this.search_string = this.search_string.trim().toLowerCase();
        }
                
    }

    /** Returns the patient id based on the parameter values
     * 
     * @param surname
     * @param name
     * @param email
     * @param telephone
     * @param address
     * @param comment
     * @return
     */            
    public int GetPatientID (String id) {
        if (patients == null || patients.size() == 0) return -1;
        int pat_id = -1;
        try {
            pat_id = Integer.parseInt(id);
        } catch (Exception e) {
            
        }
        for (int x = 0; x < patients.size(); x++) {
            if (patients.get(x).id == pat_id) {
                return x;
            }
        }
        return -1;
    }

    /** Updates the patients table
     * 
     */
    public void UpdatePatientsTable(boolean read_data_from_file, int search_in, String search_string) {
        GeneratePatientTableThread gpt = new GeneratePatientTableThread(read_data_from_file, search_in, search_string);
        gpt.run();        
    }
    
   
    
    /** Creates new form PMSDialog */
    public PMSDialog(java.awt.Frame parent, boolean modal, String patient_filename, String pms_path, PrescriberView open_rep_form) {
        super(parent, modal); 
        this.parent = parent;
        this.pms_path = pms_path;
        this.open_rep_form = open_rep_form;
        initComponents();

        PatientTable.getSelectionModel().addListSelectionListener(PatientSelectionModel);

        int size = open_rep_form.config.GetValue(Configuration.Key_PMSDialog_PatientTable);

        if (size != -1) {
            Font ft = new Font(PatientTable.getFont().getName(), PatientTable.getFont().getStyle(), size);
            PatientTable.setFont(ft);
            PatientTable.setRowHeight(PatientTable.getFontMetrics(PatientTable.getFont()).getHeight());
        }

        /** This mouselistener is used to listen to clicks on the RemSymptomTable header
        * 
        */
        MouseListener PatientTableHeaderListener = new MouseListener() {

        public void mouseClicked(MouseEvent e) {
            int col = PatientTable.columnAtPoint(e.getPoint());
            if (col == sort_by_col) sort_asc = !sort_asc;
            else
            {
                sort_asc = false;
                sort_by_col = col;
            }
            GeneratePatientTableThread temp;
            if (!SearchButton.isSelected() || SearchEdit.getText().trim().equals(""))
            temp = new GeneratePatientTableThread(false, -1, null);
            else
            temp = new GeneratePatientTableThread(false, SearchInComboBox.getSelectedIndex(), SearchEdit.getText());
            temp.run();
                    
            
        }

        public void mousePressed(MouseEvent e) {
            
        }

        public void mouseReleased(MouseEvent e) {
            
        }

        public void mouseEntered(MouseEvent e) {
            
        }

        public void mouseExited(MouseEvent e) {
            
        }

        };
        
        PatientTable.getTableHeader().addMouseListener(PatientTableHeaderListener);
        this.setTitle("Patientenverwaltung");
        this.patient_filename = patient_filename;
        ReadPatientFile(patient_filename);
        UpdatePatientsTable(true, -1, null);
        PatientTable.getSelectionModel().setSelectionInterval(0, 0);
    }

    /** Updates the TextArea that contains the additional information
     *
     * @param row_nr
     */
    public void UpdateAdditionalInformationTextArea (int row_nr) {
        try {
            selected_patient_id = GetPatientID(PatientTable.getModel().getValueAt(row_nr, 0).toString());
        } catch (Exception e) {}
        if (selected_patient_id == -1) return;
    }

    ListSelectionListener PatientSelectionModel = new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {
            UpdateAdditionalInformationTextArea(PatientTable.getSelectedRow());
        }
    };


/** Is used to provide a model for the RemSymptomTable
 *
 * @author vladimir
 */
class MyModel extends DefaultTableModel {

    private String[][] data;
    private String[] columns;

    MyModel (String[][] data, String[] columnnames) {
        this.data = data;
        this.columns = columnnames;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    @Override
    public String getColumnName(int column) {
        return this.columns[column];
    }

    @Override
    public int getColumnCount() {
        if (data.length == 0) return 0;
        return data[0].length;
    }

    @Override
    public int getRowCount() {
        if (data == null) return 0;
        return data.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
    /** Returns a new patient id
     * 
     * @return
     */
    public int GetNewPatientID () {
        int patient_id = 0;
        for (int x = 0; x < patients.size(); x++) {
            if (patients.get(x).id > patient_id) patient_id = patients.get(x).id;
        }
        return ++patient_id;
    }
    
    /** Writes the contents of the patients ArrayList to a file
     * 
     * @param filename
     */
    public void WritePatientFile (String filename) {
        Logger.AddInitEntry(Logger.Operation_ReadPatients, filename);
        try {
            if (!(new File (filename).exists())) {
                new File (filename).createNewFile();
            }
            {
                ArrayList<String> content = new ArrayList();
                for (int x = 0; x < patients.size(); x++) {
                    content.add(Database.PATIENT_FILE_PATIENT_TAG_START+"\n");
                    content.add("\t"+Database.PATIENT_FILE_ID_TAG_START+patients.get(x).id+Database.PATIENT_FILE_ID_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_NAME_TAG_START+patients.get(x).name+Database.PATIENT_FILE_NAME_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_SURNAME_TAG_START+patients.get(x).surname+Database.PATIENT_FILE_SURNAME_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_ADDRESS_TAG_START+patients.get(x).address+Database.PATIENT_FILE_ADDRESS_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_TELEPHONE_TAG_START+patients.get(x).telephone+Database.PATIENT_FILE_TELEPHONE_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_EMAIL_TAG_START+patients.get(x).email+Database.PATIENT_FILE_EMAIL_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_COMMENT_TAG_START+patients.get(x).comment+Database.PATIENT_FILE_COMMENT_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_BIRTH_TAG_START+patients.get(x).date_of_birth+Database.PATIENT_FILE_BIRTH_TAG_END+"\n");
                    String sex = "M";
                    if (!patients.get(x).sex) sex = "F";
                    content.add("\t"+Database.PATIENT_FILE_SEX_TAG_START+sex+Database.PATIENT_FILE_SEX_TAG_END+"\n");
                    content.add("\t"+Database.PATIENT_FILE_ADDITIONALINFORMATION_TAG_START+patients.get(x).additional_information+Database.PATIENT_FILE_ADDITIONALINFORMATION_TAG_END+"\n");
                    content.add(Database.PATIENT_FILE_PATIENT_TAG_END+"\n");
                }
                Utils.WriteFile(filename, content, true);
            }
        }
        catch (Exception e) {
            this.setVisible(false);            
            JOptionPane.showMessageDialog(rootPane, "Beim Schreiben der PMS-Datenbank kam es zu einem Fehler."+e.getMessage());
            Logger.AddFailureEntry(Logger.Operation_ReadPatients, e.getMessage());
            this.setVisible(true);
            return;
        }             
    }
    
    /** Reads the xml file containing information about patients
     * 
     * @param filename
     */
    private void ReadPatientFile (String filename) {
        Logger.AddInitEntry(Logger.Operation_ReadPatients, filename);
        if (!new File (filename).exists()) {
            try{
                new File (filename).createNewFile();
            }
            catch (Exception e) {}
        }
        try {
            String data = Utils.ReadFile(filename, "\n");
            Logger.AddSuccessEntry(Logger.Operation_ReadPatients, "");
            patients.clear();
            ArrayList<String> patients_info = Utils.ReadTagContents(data, Database.PATIENT_FILE_PATIENT_TAG_START, Database.PATIENT_FILE_PATIENT_TAG_END);
            for (int x = 0; x < patients_info.size(); x++) {
                if (patients_info.get(x) != null && !patients_info.get(x).equals("")) {
                    Patient p = new Patient();
                    p.address = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_ADDRESS_TAG_START, Database.PATIENT_FILE_ADDRESS_TAG_END, 0);
                    p.comment = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_COMMENT_TAG_START, Database.PATIENT_FILE_COMMENT_TAG_END, 0);
                    p.email = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_EMAIL_TAG_START, Database.PATIENT_FILE_EMAIL_TAG_END, 0);
                    p.name = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_NAME_TAG_START, Database.PATIENT_FILE_NAME_TAG_END, 0);
                    p.surname = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_SURNAME_TAG_START, Database.PATIENT_FILE_SURNAME_TAG_END, 0);
                    p.telephone = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_TELEPHONE_TAG_START, Database.PATIENT_FILE_TELEPHONE_TAG_END, 0);
                    p.additional_information = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_ADDITIONALINFORMATION_TAG_START, Database.PATIENT_FILE_ADDITIONALINFORMATION_TAG_END, 0);
                    p.date_of_birth = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_BIRTH_TAG_START, Database.PATIENT_FILE_BIRTH_TAG_END, 0);
                    String temp = Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_SEX_TAG_START, Database.PATIENT_FILE_SEX_TAG_END, 0);
                    if (temp == null || temp.equals("M")) p.sex = true;
                    else
                    p.sex = false;
                    try {
                        p.id = Integer.parseInt(Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_ID_TAG_START, Database.PATIENT_FILE_ID_TAG_END, 0));
                        patients.add(p);
                    } catch (Exception e) {
                        Logger.AddFailureEntry(Logger.Operation_ReadPatients, "Fehler beim Parsen der Patienten ID. Wert=\""+Utils.ReadTag(patients_info.get(x), Database.PATIENT_FILE_ID_TAG_START, Database.PATIENT_FILE_ID_TAG_END, 0)+"\"");
                    }
                }
            }
        }
        catch (Exception e) {
            this.setVisible(false);
            JOptionPane.showMessageDialog(rootPane, "Es gab einen Fehler beim Laden der PMS-Datenbank."+e.getMessage());
            Logger.AddFailureEntry(Logger.Operation_ReadPatients, e.getMessage());
            this.setVisible(true);
            return;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        PatientTable = new javax.swing.JTable();
        AddButton = new javax.swing.JButton();
        EditButton = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        DiagnosisButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        SearchInComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        SearchEdit = new javax.swing.JTextField();
        SearchButton = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(prescriber.PrescriberApp.class).getContext().getResourceMap(PMSDialog.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        PatientTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        PatientTable.setName("PatientTable"); // NOI18N
        PatientTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                PatientTablePropertyChange(evt);
            }
        });
        PatientTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PatientTableKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(PatientTable);

        AddButton.setText(resourceMap.getString("AddButton.text")); // NOI18N
        AddButton.setName("AddButton"); // NOI18N
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });

        EditButton.setText(resourceMap.getString("EditButton.text")); // NOI18N
        EditButton.setName("EditButton"); // NOI18N
        EditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditButtonActionPerformed(evt);
            }
        });

        DeleteButton.setText(resourceMap.getString("DeleteButton.text")); // NOI18N
        DeleteButton.setMaximumSize(new java.awt.Dimension(74, 29));
        DeleteButton.setMinimumSize(new java.awt.Dimension(74, 29));
        DeleteButton.setName("DeleteButton"); // NOI18N
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });

        DiagnosisButton.setFont(resourceMap.getFont("DiagnosisButton.font")); // NOI18N
        DiagnosisButton.setForeground(resourceMap.getColor("DiagnosisButton.foreground")); // NOI18N
        DiagnosisButton.setText(resourceMap.getString("DiagnosisButton.text")); // NOI18N
        DiagnosisButton.setName("DiagnosisButton"); // NOI18N
        DiagnosisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DiagnosisButtonActionPerformed(evt);
            }
        });

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        SearchInComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ALLES", "ID", "Vorname", "Name", "E-Mail", "Telefon", "Addresse", "Kommentar" }));
        SearchInComboBox.setName("SearchInComboBox"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        SearchEdit.setText(resourceMap.getString("SearchEdit.text")); // NOI18N
        SearchEdit.setName("SearchEdit"); // NOI18N
        SearchEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SearchEditKeyPressed(evt);
            }
        });

        SearchButton.setFont(resourceMap.getFont("SearchButton.font")); // NOI18N
        SearchButton.setForeground(resourceMap.getColor("SearchButton.foreground")); // NOI18N
        SearchButton.setText(resourceMap.getString("SearchButton.text")); // NOI18N
        SearchButton.setName("SearchButton"); // NOI18N
        SearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 605, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(DeleteButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(EditButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(AddButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, Short.MAX_VALUE)))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SearchInComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SearchEdit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(SearchButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 98, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 134, Short.MAX_VALUE)
                .add(DiagnosisButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(AddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(EditButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 55, Short.MAX_VALUE)
                        .add(DeleteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(130, 130, 130))
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(SearchEdit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(SearchInComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)
                    .add(SearchButton)
                    .add(DiagnosisButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void PatientTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_PatientTablePropertyChange
    
}//GEN-LAST:event_PatientTablePropertyChange

private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
   PatientDialog pd = new PatientDialog(null, true, open_rep_form);
   pd.setLocationRelativeTo(parent);
   this.setVisible(false);
   pd.setVisible(true);
   try {
    if (pd.result == null) return;
    pd.result.id = GetNewPatientID();
    patients.add(pd.result);
    UpdatePatientsTable(false, -1, null);
    WritePatientFile(patient_filename);
   } finally {
        this.setVisible(true);       
   }
}//GEN-LAST:event_AddButtonActionPerformed

private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
   if (patients.size() == 0) return;
   if (PatientTable.getSelectedRow() == -1) return;
   this.setVisible(false);
   try{
        int result = JOptionPane.showConfirmDialog(null, "Do you really want to delete the patient ?");
        if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) return;
        File f = new File(pms_path+selected_patient_id+".xml");
        f.delete();
        patients.remove(selected_patient_id);
        UpdatePatientsTable(false, -1, null);
        WritePatientFile(patient_filename);
   } finally {
       this.setVisible(true);
   }
}//GEN-LAST:event_DeleteButtonActionPerformed

private void EditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditButtonActionPerformed
   if (selected_patient_id == -1 || selected_patient_id >= patients.size()) return;
   PatientDialog pd = new PatientDialog (null, true, open_rep_form);
   pd.setLocationRelativeTo(parent);
   pd.SetData(patients.get(selected_patient_id));
   this.setVisible(false);
   pd.setVisible(true);
   try {
        if (pd.result == null) return;
        pd.result.id = patients.get(selected_patient_id).id;
        patients.get(selected_patient_id).DeepCopy(pd.result);
        UpdatePatientsTable(false, -1, null);
        WritePatientFile(patient_filename);   
   } finally {
       this.setVisible(true);
   }
}//GEN-LAST:event_EditButtonActionPerformed

private void DiagnosisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DiagnosisButtonActionPerformed
   if (selected_patient_id == -1) return;
   if (diagnosis_dialog != null) diagnosis_dialog.setVisible(false);
   diagnosis_dialog = new PatientDiagnosisDialog(null, false, pms_path+selected_patient_id+".xml", patients.get(selected_patient_id), open_rep_form, this);
   diagnosis_dialog.setLocationRelativeTo(parent);
   this.setVisible(false);
   diagnosis_dialog.setVisible(true);
}//GEN-LAST:event_DiagnosisButtonActionPerformed

private void SearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchButtonActionPerformed
   if (!SearchButton.isSelected() || SearchEdit.getText().trim().equals("")) UpdatePatientsTable(false, -1, null);
   else
   UpdatePatientsTable(false, SearchInComboBox.getSelectedIndex(), SearchEdit.getText());
}//GEN-LAST:event_SearchButtonActionPerformed

private void SearchEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SearchEditKeyPressed
   if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
       SearchButton.setSelected(true);
       SearchButtonActionPerformed(null);
   }
}//GEN-LAST:event_SearchEditKeyPressed

private void PatientTableKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PatientTableKeyPressed
    if (evt.isAltDown()) {
        if (evt.getKeyCode() == evt.VK_UP) {
            Utils.ChangeFont(PatientTable, 1);
            open_rep_form.config.SetValue(Configuration.Key_PMSDialog_PatientTable, PatientTable.getFont().getSize());
            PatientTable.setRowHeight(PatientTable.getFontMetrics(PatientTable.getFont()).getHeight());
        }
        else
        if (evt.getKeyCode() == evt.VK_DOWN) {
            Utils.ChangeFont(PatientTable, -1);
            open_rep_form.config.SetValue(Configuration.Key_PMSDialog_PatientTable, PatientTable.getFont().getSize());
            PatientTable.setRowHeight(PatientTable.getFontMetrics(PatientTable.getFont()).getHeight());
        }
    }
}//GEN-LAST:event_PatientTableKeyPressed

private void PatientTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PatientTableMouseClicked
    if (evt.getClickCount() > 1) EditButtonActionPerformed(null);
}//GEN-LAST:event_PatientTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JButton DeleteButton;
    private javax.swing.JButton DiagnosisButton;
    private javax.swing.JButton EditButton;
    public javax.swing.JTable PatientTable;
    private javax.swing.JToggleButton SearchButton;
    private javax.swing.JTextField SearchEdit;
    private javax.swing.JComboBox SearchInComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
