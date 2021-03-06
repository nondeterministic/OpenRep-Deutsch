/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PatientDiagnosisDialog.java
 *
 * Created on Mar 9, 2009, 2:01:50 AM
 */

package prescriber;

import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import javax.swing.JOptionPane;

/**
 *
 * @author vladimir
 */
public class PatientDiagnosisDialog extends javax.swing.JFrame {

    /** Contains the name of the Edit button. This is saved because the EDIT button changes to the
        SAVE button when the diagnosis is being edited. */
    private String EditButton_Edit = null;
    /** Contains the name of the Save button. This is saved because the EDIT button changes to the
        SAVE button when the diagnosis is being edited. */
    private String EditButton_Save = null;
    /** Contains the name of the Delete button. This is saved because the Delete button changes to the
        CANCEL button when the diagnosis is being edited. */
    private String DeleteButton_Delete = null;
    /** Contains the name of the Cancel button. This is saved because the EDIT button changes to the
        SAVE button when the diagnosis is being edited. */
    private String DeleteButton_Cancel = null;

    /** contains the file name where all the patient diagnosis data are being kept */
    private String patient_filename;
    /** contains the list of all diagnoses */
    private ArrayList<PatientDiagnosis> diagnoses = new ArrayList();
    /** contains the name of the patient in the format Surname, Name*/
    private String patient_name = "";
    /** contains the pointer to the OpenRep main form*/
    private PrescriberView OpenRepForm;

    /** contains the instance of the dialog that has opened this one*/
    private PMSDialog my_parent;

    /** no action is being currently performed */
    private final int ACTION_NONE = -1;
    /** adding is currently being performed */
    private final int ACTION_ADD = -2;
    /** this property is read by the pmsdialog to determine whenther to close itself (it occurs after loading a
     *  repertorization, when the user wants to display it in OpenRep */
    public boolean force_pms_quit = false;

    /** sort diagnoses by col with this index*/
    private int sort_col = 0;
    /** sort ascending */
    private boolean sort_asc = false;

    /** contains the id of the performed action, if it is not an ID of a diagnosis (meaning is lesser than 0 -> is either no action, or add) */
    private int action = ACTION_NONE;

    private int current_top_position = 0;

    private boolean diagnosis_1_changed = false;
    private boolean diagnosis_2_changed = false;
    private boolean diagnosis_3_changed = false;

    public void SetDiagnosis_1Changed(boolean chng) {
        this.diagnosis_1_changed = chng;
    }

    public void SetDiagnosis_2Changed(boolean chng) {
        this.diagnosis_2_changed = chng;
    }

    public void SetDiagnosis_3Changed(boolean chng) {
        this.diagnosis_3_changed = chng;
    }

    WindowListener wl = new WindowListener() {

        public void windowOpened(WindowEvent e) {

        }

        public void windowClosing(WindowEvent e) {
            SaveData();
            WritePatientDiagnosis(patient_filename);
            my_parent.setVisible(true);
        }

        public void windowClosed(WindowEvent e) {

        }

        public void windowIconified(WindowEvent e) {

        }

        public void windowDeiconified(WindowEvent e) {

        }

        public void windowActivated(WindowEvent e) {

        }

        public void windowDeactivated(WindowEvent e) {
        }
    };

    /** Reads the diagnosis file
     *
     * @param filename
     */
    private void ReadPatientDiagnosis(String filename) {
        if (!(new File(filename).exists())) return;
        Logger.AddInitEntry(Logger.Operation_ReadDiagnoses, filename);
        try {
            String data = Utils.ReadFile(filename, "\n");
            Logger.AddSuccessEntry(Logger.Operation_ReadDiagnoses, "");
            diagnoses.clear();
            ArrayList<String> diagnosis_info = Utils.ReadTagContents(data, Database.PATIENTDATA_FILE_DATA_TAG_START, Database.PATIENTDATA_FILE_DATA_TAG_END);
            for (int x = 0; x < diagnosis_info.size(); x++) {
                if (diagnosis_info.get(x) != null && !diagnosis_info.get(x).equals("")) {
                    PatientDiagnosis pd = new PatientDiagnosis();
                    pd.date = Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_DATE_TAG_START, Database.PATIENTDATA_FILE_DATE_TAG_END, 0);
                    pd.description = Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_DESCRIPTION_TAG_START, Database.PATIENTDATA_FILE_DESCRIPTION_TAG_END, 0);
                    pd.short_description = Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_SHORTDESCRIPTION_TAG_START, Database.PATIENTDATA_FILE_SHORTDESCRIPTION_TAG_END, 0);
                    Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_REMEDY_TAG_START, Database.PATIENTDATA_FILE_REMEDY_TAG_END, 0);
                    String temps = Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_PRESCRIPTIONS_TAG_START, Database.PATIENTDATA_FILE_PRESCRIPTIONS_TAG_END, 0);
                    pd.SetPrescriptions(temps);
                    String appendices = Utils.ReadTag(diagnosis_info.get(x), Database.PATIENTDATA_FILE_APPENDICES_TAG_START, Database.PATIENTDATA_FILE_APPENDICES_TAG_END, 0);
                    if (appendices != null && !appendices.trim().equals("")) {
                        ArrayList<String> appendices_contents = Utils.ReadTagContents(appendices, Database.PATIENTDATA_APPENDIX_TAG_START, Database.PATIENTDATA_APPENDIX_TAG_END);
                        for (int y = 0; y < appendices_contents.size(); y++) {
                            PatientAppendix pa = new PatientAppendix();
                            pa.description = Utils.ReadTag(appendices_contents.get(y), Database.PATIENTDATA_APPENDIXNAME_TAG_START, Database.PATIENTDATA_APPENDIXNAME_TAG_END, 0);
                            pa.filename = Utils.ReadTag(appendices_contents.get(y), Database.PATIENTDATA_APPENDIXFILE_TAG_START, Database.PATIENTDATA_APPENDIXFILE_TAG_END, 0);
                            pd.appendices.add(pa);
                        }
                    }
                    diagnoses.add(pd);
                }
            }
            Collections.sort(diagnoses);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "There was an error while opening the PMS database");
            Logger.AddFailureEntry(Logger.Operation_ReadPatients, e.getMessage());
            return;
        }
    }

    /** Writes the diagnosis file
     *
     * @param filename
     */
    private void WritePatientDiagnosis(String filename) {
        Logger.AddInitEntry(Logger.Operation_WriteDiagnoses, filename);
        if (!(new File(filename).exists())) {
            try {
                new File (filename).createNewFile();
                Logger.AddSuccessEntry(Logger.Operation_WriteDiagnoses, "File did not exist... created \""+filename+"\"");
            }
            catch (Exception e) {
                Logger.AddFailureEntry(Logger.Operation_WriteDiagnoses, "File did not exist... could not create \""+filename+"\"");
                JOptionPane.showMessageDialog(rootPane, "Could not create the diagnosis file. Data were not save "+e.getMessage());
                return;
            }
        }
        try {
            ArrayList<String> diagnosis_info = new ArrayList();
            for (int x = 0; x < diagnoses.size(); x++) {
                diagnosis_info.add(Database.PATIENTDATA_FILE_DATA_TAG_START+"\n");
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_DATE_TAG_START+diagnoses.get(x).date+Database.PATIENTDATA_FILE_DATE_TAG_END+"\n");
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_SHORTDESCRIPTION_TAG_START+diagnoses.get(x).short_description+Database.PATIENTDATA_FILE_SHORTDESCRIPTION_TAG_END+"\n");
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_DESCRIPTION_TAG_START+diagnoses.get(x).description+Database.PATIENTDATA_FILE_DESCRIPTION_TAG_END+"\n");
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_PRESCRIPTIONS_TAG_START+diagnoses.get(x).GetPrescriptions()+Database.PATIENTDATA_FILE_PRESCRIPTIONS_TAG_END+"\n");
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_APPENDICES_TAG_START+"\n");
                for (int y = 0; y < diagnoses.get(x).appendices.size(); y++) {
                    diagnosis_info.add("\t\t"+Database.PATIENTDATA_APPENDIX_TAG_START+"\n");
                    diagnosis_info.add("\t\t"+Database.PATIENTDATA_APPENDIXNAME_TAG_START+diagnoses.get(x).appendices.get(y).description+Database.PATIENTDATA_APPENDIXNAME_TAG_END+"\n");
                    diagnosis_info.add("\t\t"+Database.PATIENTDATA_APPENDIXFILE_TAG_START+diagnoses.get(x).appendices.get(y).filename+Database.PATIENTDATA_APPENDIXFILE_TAG_END+"\n");
                    diagnosis_info.add("\t\t"+Database.PATIENTDATA_APPENDIX_TAG_END+"\n");
                }
                diagnosis_info.add("\t"+Database.PATIENTDATA_FILE_APPENDICES_TAG_END+"\n");
                diagnosis_info.add(Database.PATIENTDATA_FILE_DATA_TAG_END+"\n");
            }
            Utils.WriteFile(filename, diagnosis_info, true);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "There was an error while writing the PMS diagnosis database file."+e.getMessage());
            Logger.AddFailureEntry(Logger.Operation_WritePatients, e.getMessage());
            return;
        }
    }

    private ArrayList<PatientDiagnosis> SortDiagnosis (int sort_col, boolean asc, ArrayList<PatientDiagnosis> pat) {
            ArrayList<PatientDiagnosis> result = new ArrayList();
            ArrayList<String> sort_criteria = new ArrayList();
            for (int x = 0; x < pat.size(); x++) {
                if (sort_col == 0) sort_criteria.add(pat.get(x).date);
                else
                if (sort_col == 1) sort_criteria.add(pat.get(x).description);
                else
                if (sort_col == 2) sort_criteria.add(pat.get(x).GetPrescriptionText());
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
                        if (sort_col == 0 && pat.get(y).date.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 1 && pat.get(y).description.equals(sort_criteria.get(x))) {
                            result.add(pat.get(y));
                            pat.remove(y);
                            break;
                        }
                        else
                        if (sort_col == 2 && pat.get(y).GetPrescriptionText().equals(sort_criteria.get(x))) {
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

    /** Returns the current_top_position
     * 
     * @return
     */
    public int GetCurrentTopPosition() {
        return this.current_top_position;
    }

    /** Updates the contents of the edits
     * 
     * @param top_position
     */
    public void UpdateContents(int top_position) {
        diagnosis_1.setText("");
        prescription_1.setText("");

        diagnosis_2.setText("");
        prescription_2.setText("");

        diagnosis_3.setText("");
        prescription_3.setText("");

        date_label_1.setText("Now");
        time_label_1.setText("Current");

        date_label_2.setText("Now");
        time_label_2.setText("Current");

        date_label_3.setText("Now");
        time_label_3.setText("Current");

        DateEditButton_3.setEnabled(false);

        if (top_position <= diagnoses.size() - 1) {
            diagnosis_1.setText(diagnoses.get(top_position).description);
            prescription_1.setText(diagnoses.get(top_position).prescription);
            String[] temps = diagnoses.get(top_position).date.split(" ");
            date_label_1.setText(temps[0]);
            time_label_1.setText(temps[1]);
        }

        if (top_position+1 <= diagnoses.size() - 1) {
            diagnosis_2.setText(diagnoses.get(top_position+1).description);
            prescription_2.setText(diagnoses.get(top_position+1).prescription);
            String[] temps = diagnoses.get(top_position+1).date.split(" ");
            date_label_2.setText(temps[0]);
            time_label_2.setText(temps[1]);
        }

        if (top_position+2 <= diagnoses.size() - 1) {
            diagnosis_3.setText(diagnoses.get(top_position+2).description);
            prescription_3.setText(diagnoses.get(top_position+2).prescription);
            String[] temps = diagnoses.get(top_position+2).date.split(" ");
            date_label_3.setText(temps[0]);
            time_label_3.setText(temps[1]);
            DateEditButton_3.setEnabled(true);
        }
        UpdateDisplayRepertorizationButtons(current_top_position);
    }

    /** Saves data from edits to the data structure
     * 
     */
    public void SaveData() {
        if (diagnosis_1_changed) {
            try {
                diagnoses.get(current_top_position).description = diagnosis_1.getText();
                diagnoses.get(current_top_position).prescription = prescription_1.getText();
            } catch (Exception e) {}
        }
        if (diagnosis_2_changed) {
            try {
                diagnoses.get(current_top_position+1).description = diagnosis_2.getText();
                diagnoses.get(current_top_position+1).prescription = prescription_2.getText();
            } catch (Exception e) {}
        }
        if (diagnosis_3_changed) {
            try {
                diagnoses.get(current_top_position+2).description = diagnosis_3.getText();
                diagnoses.get(current_top_position+2).prescription = prescription_3.getText();
            } catch (Exception e) {}
        }
        diagnosis_1_changed = false;
        diagnosis_2_changed = false;
        diagnosis_3_changed = false;
    }

    /** Updates the DisplayRepertorization Buttons according to whether the diagnosis contains repertorization or not
     * 
     * @param top_position
     */
    public void UpdateDisplayRepertorizationButtons(int top_position) {
        display_repertorization_1.setEnabled(false);
        display_repertorization_2.setEnabled(false);
        display_repertorization_3.setEnabled(false);
        try {
            if (diagnoses.get(top_position).appendices.size() != 0) display_repertorization_1.setEnabled(true);
        }
        catch (Exception e) {}
        try {
            if (diagnoses.get(top_position+1).appendices.size() != 0) display_repertorization_2.setEnabled(true);
        }
        catch (Exception e) {}
        try {
            if (diagnoses.get(top_position+2).appendices.size() != 0) display_repertorization_3.setEnabled(true);
        }
        catch (Exception e) {}
    }

    /** Updates the edits
     * 
     * @param top_position
     */
    private void UpdateEdits(int top_position) {
        // disable the bottom 2 edits
        /*diagnosis_2.setEnabled(false);
        diagnosis_3.setEnabled(false);

        prescription_2.setEnabled(false);
        prescription_3.setEnabled(false);

        add_repertorization_2.setEnabled(false);
        add_repertorization_3.setEnabled(false);

        delete_diagnosis_2.setEnabled(false);
        delete_diagnosis_3.setEnabled(false);

        delete_diagnosis_2.setEnabled(false);
        delete_diagnosis_3.setEnabled(false);

        display_repertorization_2.setEnabled(false);
        display_repertorization_3.setEnabled(false);*/

        if (top_position + 2 <= diagnoses.size() - 1) {
            add_repertorization_1.setEnabled(true);
            add_repertorization_2.setEnabled(true);
            add_repertorization_3.setEnabled(true);
            delete_diagnosis_1.setEnabled(true);
            delete_diagnosis_2.setEnabled(true);
            delete_diagnosis_3.setEnabled(true);
            display_repertorization_1.setEnabled(true);
            display_repertorization_2.setEnabled(true);
            display_repertorization_3.setEnabled(true);

            DateEditButton_1.setEnabled(true);
            DateEditButton_2.setEnabled(true);
            DateEditButton_3.setEnabled(true);

            diagnosis_2.setEnabled(true);
            diagnosis_3.setEnabled(true);

            prescription_2.setEnabled(true);
            prescription_3.setEnabled(true);

            add_repertorization_2.setEnabled(true);
            add_repertorization_3.setEnabled(true);

            delete_diagnosis_2.setEnabled(true);
            delete_diagnosis_3.setEnabled(true);

            delete_diagnosis_2.setEnabled(true);
            delete_diagnosis_3.setEnabled(true);

            display_repertorization_2.setEnabled(true);
            display_repertorization_3.setEnabled(true);
            UpdateDisplayRepertorizationButtons(top_position);
        }

        // there is nothing to display (the top edits are enabled the others disabled)
        if (top_position == -1 || top_position > diagnoses.size() - 1) {
            date_label_1.setText("Now");
            time_label_1.setText("Current");
            prescription_1.setText("");
            diagnosis_1.setText("");
            add_repertorization_1.setEnabled(true);
            delete_diagnosis_1.setEnabled(true);
            display_repertorization_1.setEnabled(true);

            DateEditButton_1.setEnabled(false);
            DateEditButton_2.setEnabled(false);
            DateEditButton_3.setEnabled(false);

            date_label_2.setText("");
            date_label_3.setText("");

            time_label_2.setText("");
            time_label_3.setText("");

            diagnosis_2.setEnabled(false);
            diagnosis_3.setEnabled(false);

            prescription_2.setEnabled(false);
            prescription_3.setEnabled(false);

            add_repertorization_2.setEnabled(false);
            add_repertorization_3.setEnabled(false);

            delete_diagnosis_2.setEnabled(false);
            delete_diagnosis_3.setEnabled(false);

            delete_diagnosis_2.setEnabled(false);
            delete_diagnosis_3.setEnabled(false);

            display_repertorization_2.setEnabled(false);
            display_repertorization_3.setEnabled(false);

            UpdateDisplayRepertorizationButtons(top_position);
            return;
        }

        if (top_position+1 > diagnoses.size() - 1) {
            
            date_label_2.setText("Now");
            time_label_2.setText("Current");
            diagnosis_2.setEnabled(true);
            prescription_2.setEnabled(true);
            prescription_2.setText("");
            diagnosis_2.setText("");
            add_repertorization_2.setEnabled(true);
            delete_diagnosis_2.setEnabled(true);
            display_repertorization_2.setEnabled(true);

            DateEditButton_1.setEnabled(true);
            DateEditButton_2.setEnabled(false);
            DateEditButton_3.setEnabled(false);

            time_label_3.setText("");

            time_label_3.setText("");

            diagnosis_3.setEnabled(false);

            prescription_3.setEnabled(false);

            add_repertorization_3.setEnabled(false);

            delete_diagnosis_3.setEnabled(false);

            delete_diagnosis_3.setEnabled(false);

            display_repertorization_3.setEnabled(false);

            UpdateDisplayRepertorizationButtons(top_position);
            return;
        }

        if (top_position+2 > diagnoses.size() - 1) {
            diagnosis_2.setEnabled(true);
            prescription_2.setEnabled(true);
            add_repertorization_2.setEnabled(true);
            delete_diagnosis_2.setEnabled(true);
            display_repertorization_2.setEnabled(true);

            DateEditButton_1.setEnabled(true);
            DateEditButton_2.setEnabled(true);

            date_label_3.setText("Now");
            time_label_3.setText("Current");
            diagnosis_3.setEnabled(true);
            prescription_3.setEnabled(true);
            prescription_3.setText("");
            diagnosis_3.setText("");
            add_repertorization_3.setEnabled(true);
            delete_diagnosis_3.setEnabled(true);
            display_repertorization_3.setEnabled(true);

            UpdateDisplayRepertorizationButtons(top_position);
            return;
        }
        
    }

    /** Evaluated whether the diagnosis specified in the parameters has changed or not
     *  
     */
    public void EvaluateDiagnosisChange (int diagnosis_id, boolean force_create) {
        System.out.println(diagnosis_id);
        boolean changed = false;
        String diagnosis_text = "";
        String prescription_text = "";
        if (diagnosis_id == 0) {
            diagnosis_text = diagnosis_1.getText();
            prescription_text = prescription_1.getText();
        }
        else
        if (diagnosis_id == 1) {
            diagnosis_text = diagnosis_2.getText();
            prescription_text = prescription_2.getText();
        }
        else
        if (diagnosis_id == 2) {
            diagnosis_text = diagnosis_3.getText();
            prescription_text = prescription_3.getText();
        }
        if (current_top_position+diagnosis_id > diagnoses.size() - 1) {
            if (diagnosis_text.equals("") && prescription_text.equals("") && !force_create) return;
            changed = true;
            PatientDiagnosis pd = new PatientDiagnosis();
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String[] temps = sdf.format(cal.getTime()).split(" ");
            pd.date = sdf.format(cal.getTime());
            pd.description = "";
            pd.prescription = "";
            diagnoses.add(pd);
            if (diagnosis_id == 2) {
                System.out.println("THIS");
                boolean diag = false;
                boolean pres = false;
                diag = diagnosis_3.isFocusOwner();
                pres = prescription_3.isFocusOwner();
                diagnosis_3_changed = true;
                SaveData();
                current_top_position++;
                UpdateContents(current_top_position);
                UpdateEdits(current_top_position);
                if (diag) diagnosis_2.grabFocus();
                if (pres) prescription_2.grabFocus();
                return;
            }
            if (diagnosis_id == 0) {
                date_label_1.setText(temps[0]);
                time_label_1.setText(temps[1]);
            }
            if (diagnosis_id == 1) {
                date_label_2.setText(temps[0]);
                time_label_2.setText(temps[1]);
            }
            if (diagnosis_id == 2) {
                date_label_3.setText(temps[0]);
                time_label_3.setText(temps[1]);
            }
        }
        else {
            if (diagnoses.get(current_top_position + diagnosis_id).description != null && diagnoses.get(current_top_position + diagnosis_id).description.equals(diagnosis_text)) changed = true;
            if (diagnoses.get(current_top_position + diagnosis_id).prescription != null && diagnoses.get(current_top_position + diagnosis_id).prescription.equals(prescription_text)) changed = true;
        }

        UpdateEdits(current_top_position);

        if (changed) {
            if (diagnosis_id == 0) diagnosis_1_changed = true;
            if (diagnosis_id == 1) diagnosis_2_changed = true;
            if (diagnosis_id == 2) diagnosis_3_changed = true;
        }

    }

public void SaveRepertorization (boolean display_confirmation, int selected_diagnosis) {
    if (diagnoses.size() != 0 && selected_diagnosis == -1) selected_diagnosis = 0;
    for (int y = 0; y < diagnoses.get(selected_diagnosis).appendices.size(); y++) {
        File df = new File (diagnoses.get(selected_diagnosis).appendices.get(y).filename);
        df.delete();
    }
    diagnoses.get(selected_diagnosis).appendices.clear();
    int x = 0;
    String file = Utils.GetFileNameWithoutExt(patient_filename);
    while (true) {
        File f = new File(file+"_"+x+".xml");
        if (!f.exists()) break;
        x++;
    }

    if (OpenRepForm.SaveCurrentRepertorization(file+"_"+x+".xml")) {
        PatientAppendix pa = new PatientAppendix();
        pa.filename = file+"_"+x+".xml";
        pa.description = "repertorization";
        diagnoses.get(selected_diagnosis).appendices.add(pa);
        OpenRepForm.SetCurrentFileName(file+"_"+x+".xml");
        if (display_confirmation) JOptionPane.showMessageDialog(rootPane, "Repertorization was saved in this diagnosis");
    }
}

    /** Creates new form PatientDiagnosisDialog */
    public PatientDiagnosisDialog(java.awt.Frame parent, boolean modal, String file_name, Patient pat, PrescriberView open_rep_form, PMSDialog my_parent) {
        this.addWindowListener(wl);
        this.my_parent = my_parent;
        this.patient_filename = file_name;
        this.OpenRepForm = open_rep_form;

        initComponents();

        String sex = " (";
        if (pat.sex) sex += "M";
        else
        sex += "F";

        sex += " ";

        patient_name = pat.name + " " + pat.surname;

        String temp = pat.address;
        if (!pat.address.trim().equals("") && !pat.telephone.trim().equals("")) temp += ", " + pat.telephone;
        if (!pat.email.trim().equals("") && !temp.trim().equals("")) temp += ", " + pat.email;

        this.PatientLabel.setText(temp);

        this.setTitle(pat.name + " " + pat.surname+sex+Utils.ConvertDateToAge(pat.date_of_birth)+")");

        int size = OpenRepForm.config.GetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1);
        Font ft;
        if (size != -1) {
            ft = new Font(diagnosis_1.getFont().getName(), diagnosis_1.getFont().getStyle(), size);
            diagnosis_1.setFont(ft);
            diagnosis_2.setFont(ft);
            diagnosis_3.setFont(ft);
        }

        size = OpenRepForm.config.GetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1);
        if (size != -1) {
            ft = new Font(prescription_1.getFont().getName(), prescription_1.getFont().getStyle(), size);
            prescription_1.setFont(ft);
            prescription_2.setFont(ft);
            prescription_3.setFont(ft);
        }

        ReadPatientDiagnosis(patient_filename);

        current_top_position = diagnoses.size() - 2;

        if (current_top_position < 0) current_top_position = 0;

        UpdateEdits (current_top_position);

        UpdateContents(current_top_position);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        previous_button = new javax.swing.JButton();
        next_button = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        date_label_1 = new javax.swing.JLabel();
        time_label_1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        diagnosis_1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        diagnosis_2 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        diagnosis_3 = new javax.swing.JTextArea();
        date_label_2 = new javax.swing.JLabel();
        time_label_2 = new javax.swing.JLabel();
        date_label_3 = new javax.swing.JLabel();
        time_label_3 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        prescription_1 = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        prescription_2 = new javax.swing.JTextArea();
        jScrollPane6 = new javax.swing.JScrollPane();
        prescription_3 = new javax.swing.JTextArea();
        add_repertorization_1 = new javax.swing.JButton();
        add_repertorization_2 = new javax.swing.JButton();
        add_repertorization_3 = new javax.swing.JButton();
        delete_diagnosis_1 = new javax.swing.JButton();
        delete_diagnosis_2 = new javax.swing.JButton();
        delete_diagnosis_3 = new javax.swing.JButton();
        display_repertorization_1 = new javax.swing.JButton();
        display_repertorization_2 = new javax.swing.JButton();
        display_repertorization_3 = new javax.swing.JButton();
        DateEditButton_1 = new javax.swing.JButton();
        DateEditButton_2 = new javax.swing.JButton();
        DateEditButton_3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        PatientLabel = new javax.swing.JLabel();

        setAlwaysOnTop(true);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(prescriber.PrescriberApp.class).getContext().getResourceMap(PatientDiagnosisDialog.class);
        previous_button.setIcon(resourceMap.getIcon("previous_button.icon")); // NOI18N
        previous_button.setName("previous_button"); // NOI18N
        previous_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previous_buttonActionPerformed(evt);
            }
        });

        next_button.setIcon(resourceMap.getIcon("next_button.icon")); // NOI18N
        next_button.setName("next_button"); // NOI18N
        next_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next_buttonActionPerformed(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N

        date_label_1.setText(resourceMap.getString("date_label_1.text")); // NOI18N
        date_label_1.setName("date_label_1"); // NOI18N

        time_label_1.setText(resourceMap.getString("time_label_1.text")); // NOI18N
        time_label_1.setName("time_label_1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        diagnosis_1.setColumns(20);
        diagnosis_1.setRows(5);
        diagnosis_1.setName("diagnosis_1"); // NOI18N
        diagnosis_1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                diagnosis_1MouseReleased(evt);
            }
        });
        diagnosis_1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                diagnosis_1MouseMoved(evt);
            }
        });
        diagnosis_1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                diagnosis_1InputMethodTextChanged(evt);
            }
        });
        diagnosis_1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                diagnosis_1PropertyChange(evt);
            }
        });
        diagnosis_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosis_1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                diagnosis_1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(diagnosis_1);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        diagnosis_2.setColumns(20);
        diagnosis_2.setRows(5);
        diagnosis_2.setName("diagnosis_2"); // NOI18N
        diagnosis_2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                diagnosis_2MouseMoved(evt);
            }
        });
        diagnosis_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosis_2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                diagnosis_2KeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(diagnosis_2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        diagnosis_3.setColumns(20);
        diagnosis_3.setRows(5);
        diagnosis_3.setName("diagnosis_3"); // NOI18N
        diagnosis_3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                diagnosis_3MouseMoved(evt);
            }
        });
        diagnosis_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                diagnosis_3KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                diagnosis_3KeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(diagnosis_3);

        date_label_2.setText(resourceMap.getString("date_label_2.text")); // NOI18N
        date_label_2.setName("date_label_2"); // NOI18N

        time_label_2.setText(resourceMap.getString("time_label_2.text")); // NOI18N
        time_label_2.setName("time_label_2"); // NOI18N

        date_label_3.setText(resourceMap.getString("date_label_3.text")); // NOI18N
        date_label_3.setName("date_label_3"); // NOI18N

        time_label_3.setText(resourceMap.getString("time_label_3.text")); // NOI18N
        time_label_3.setName("time_label_3"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        prescription_1.setColumns(20);
        prescription_1.setRows(5);
        prescription_1.setName("prescription_1"); // NOI18N
        prescription_1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                prescription_1MouseMoved(evt);
            }
        });
        prescription_1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                prescription_1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                prescription_1KeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(prescription_1);

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        prescription_2.setColumns(20);
        prescription_2.setRows(5);
        prescription_2.setName("prescription_2"); // NOI18N
        prescription_2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                prescription_2MouseMoved(evt);
            }
        });
        prescription_2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                prescription_2KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                prescription_2KeyReleased(evt);
            }
        });
        jScrollPane5.setViewportView(prescription_2);

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        prescription_3.setColumns(20);
        prescription_3.setRows(5);
        prescription_3.setName("prescription_3"); // NOI18N
        prescription_3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                prescription_3MouseMoved(evt);
            }
        });
        prescription_3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                prescription_3KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                prescription_3KeyReleased(evt);
            }
        });
        jScrollPane6.setViewportView(prescription_3);

        add_repertorization_1.setIcon(resourceMap.getIcon("add_repertorization_1.icon")); // NOI18N
        add_repertorization_1.setToolTipText(resourceMap.getString("add_repertorization_1.toolTipText")); // NOI18N
        add_repertorization_1.setName("add_repertorization_1"); // NOI18N
        add_repertorization_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_repertorization_1ActionPerformed(evt);
            }
        });

        add_repertorization_2.setIcon(resourceMap.getIcon("add_repertorization_2.icon")); // NOI18N
        add_repertorization_2.setToolTipText(resourceMap.getString("add_repertorization_2.toolTipText")); // NOI18N
        add_repertorization_2.setName("add_repertorization_2"); // NOI18N
        add_repertorization_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_repertorization_2ActionPerformed(evt);
            }
        });

        add_repertorization_3.setIcon(resourceMap.getIcon("add_repertorization_3.icon")); // NOI18N
        add_repertorization_3.setToolTipText(resourceMap.getString("add_repertorization_3.toolTipText")); // NOI18N
        add_repertorization_3.setName("add_repertorization_3"); // NOI18N
        add_repertorization_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add_repertorization_3ActionPerformed(evt);
            }
        });

        delete_diagnosis_1.setIcon(resourceMap.getIcon("delete_diagnosis_1.icon")); // NOI18N
        delete_diagnosis_1.setToolTipText(resourceMap.getString("delete_diagnosis_1.toolTipText")); // NOI18N
        delete_diagnosis_1.setName("delete_diagnosis_1"); // NOI18N
        delete_diagnosis_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_diagnosis_1ActionPerformed(evt);
            }
        });

        delete_diagnosis_2.setIcon(resourceMap.getIcon("delete_diagnosis_2.icon")); // NOI18N
        delete_diagnosis_2.setToolTipText(resourceMap.getString("delete_diagnosis_2.toolTipText")); // NOI18N
        delete_diagnosis_2.setName("delete_diagnosis_2"); // NOI18N
        delete_diagnosis_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_diagnosis_2ActionPerformed(evt);
            }
        });

        delete_diagnosis_3.setIcon(resourceMap.getIcon("delete_diagnosis_3.icon")); // NOI18N
        delete_diagnosis_3.setToolTipText(resourceMap.getString("delete_diagnosis_3.toolTipText")); // NOI18N
        delete_diagnosis_3.setName("delete_diagnosis_3"); // NOI18N
        delete_diagnosis_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_diagnosis_3ActionPerformed(evt);
            }
        });

        display_repertorization_1.setIcon(resourceMap.getIcon("display_repertorization_1.icon")); // NOI18N
        display_repertorization_1.setToolTipText(resourceMap.getString("display_repertorization_1.toolTipText")); // NOI18N
        display_repertorization_1.setName("display_repertorization_1"); // NOI18N
        display_repertorization_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                display_repertorization_1ActionPerformed(evt);
            }
        });

        display_repertorization_2.setIcon(resourceMap.getIcon("display_repertorization_2.icon")); // NOI18N
        display_repertorization_2.setToolTipText(resourceMap.getString("display_repertorization_2.toolTipText")); // NOI18N
        display_repertorization_2.setName("display_repertorization_2"); // NOI18N
        display_repertorization_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                display_repertorization_2ActionPerformed(evt);
            }
        });

        display_repertorization_3.setIcon(resourceMap.getIcon("display_repertorization_3.icon")); // NOI18N
        display_repertorization_3.setToolTipText(resourceMap.getString("display_repertorization_3.toolTipText")); // NOI18N
        display_repertorization_3.setName("display_repertorization_3"); // NOI18N
        display_repertorization_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                display_repertorization_3ActionPerformed(evt);
            }
        });

        DateEditButton_1.setIcon(resourceMap.getIcon("DateEditButton_1.icon")); // NOI18N
        DateEditButton_1.setName("DateEditButton_1"); // NOI18N
        DateEditButton_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DateEditButton_1ActionPerformed(evt);
            }
        });

        DateEditButton_2.setIcon(resourceMap.getIcon("DateEditButton_2.icon")); // NOI18N
        DateEditButton_2.setName("DateEditButton_2"); // NOI18N
        DateEditButton_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DateEditButton_2ActionPerformed(evt);
            }
        });

        DateEditButton_3.setIcon(resourceMap.getIcon("DateEditButton_3.icon")); // NOI18N
        DateEditButton_3.setName("DateEditButton_3"); // NOI18N
        DateEditButton_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DateEditButton_3ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(date_label_1)
                    .add(time_label_1)
                    .add(date_label_2)
                    .add(time_label_2)
                    .add(date_label_3)
                    .add(time_label_3)
                    .add(DateEditButton_1)
                    .add(DateEditButton_2)
                    .add(DateEditButton_3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 201, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 201, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 201, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(add_repertorization_1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(display_repertorization_1)
                        .add(45, 45, 45)
                        .add(delete_diagnosis_1))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(add_repertorization_2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(display_repertorization_2)
                        .add(45, 45, 45)
                        .add(delete_diagnosis_2))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(add_repertorization_3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(display_repertorization_3)
                        .add(45, 45, 45)
                        .add(delete_diagnosis_3)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane4)
                        .add(7, 7, 7)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(add_repertorization_1)
                            .add(display_repertorization_1)
                            .add(delete_diagnosis_1)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(date_label_1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(time_label_1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(DateEditButton_1))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(date_label_2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(time_label_2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(DateEditButton_2)
                        .add(55, 55, 55))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(jScrollPane5)
                                .add(7, 7, 7)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(add_repertorization_2)
                                    .add(display_repertorization_2)
                                    .add(delete_diagnosis_2)))
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane6)
                        .add(9, 9, 9)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(add_repertorization_3)
                            .add(display_repertorization_3)
                            .add(delete_diagnosis_3)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(date_label_3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(time_label_3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(DateEditButton_3))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        PatientLabel.setText(resourceMap.getString("PatientLabel.text")); // NOI18N
        PatientLabel.setName("PatientLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(PatientLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 605, Short.MAX_VALUE)
                .add(previous_button)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(next_button)
                .addContainerGap())
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(previous_button)
                    .add(next_button)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel1)
                        .add(PatientLabel)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void diagnosis_1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_diagnosis_1InputMethodTextChanged
        
    }//GEN-LAST:event_diagnosis_1InputMethodTextChanged

    private void diagnosis_1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_diagnosis_1PropertyChange
        
    }//GEN-LAST:event_diagnosis_1PropertyChange

    private void diagnosis_1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_1KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(diagnosis_1, 1);
                Utils.ChangeFont(diagnosis_2, 1);
                Utils.ChangeFont(diagnosis_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(diagnosis_1, -1);
                Utils.ChangeFont(diagnosis_2, -1);
                Utils.ChangeFont(diagnosis_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
        }
    }//GEN-LAST:event_diagnosis_1KeyPressed

    private void diagnosis_1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_diagnosis_1MouseReleased
    }//GEN-LAST:event_diagnosis_1MouseReleased

    private void diagnosis_1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_diagnosis_1MouseMoved

    }//GEN-LAST:event_diagnosis_1MouseMoved

    private void diagnosis_2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_2KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(diagnosis_1, 1);
                Utils.ChangeFont(diagnosis_2, 1);
                Utils.ChangeFont(diagnosis_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(diagnosis_1, -1);
                Utils.ChangeFont(diagnosis_2, -1);
                Utils.ChangeFont(diagnosis_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
        }
    }//GEN-LAST:event_diagnosis_2KeyPressed

    private void diagnosis_2MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_diagnosis_2MouseMoved

    }//GEN-LAST:event_diagnosis_2MouseMoved

    private void diagnosis_3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_3KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(diagnosis_1, 1);
                Utils.ChangeFont(diagnosis_2, 1);
                Utils.ChangeFont(diagnosis_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(diagnosis_1, -1);
                Utils.ChangeFont(diagnosis_2, -1);
                Utils.ChangeFont(diagnosis_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Diagnosis_1, diagnosis_1.getFont().getSize());
            }
        }
    }//GEN-LAST:event_diagnosis_3KeyPressed

    private void diagnosis_3MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_diagnosis_3MouseMoved

    }//GEN-LAST:event_diagnosis_3MouseMoved

    private void prescription_1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_1KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(prescription_1, 1);
                Utils.ChangeFont(prescription_2, 1);
                Utils.ChangeFont(prescription_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(prescription_1, -1);
                Utils.ChangeFont(prescription_2, -1);
                Utils.ChangeFont(prescription_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
        }
    }//GEN-LAST:event_prescription_1KeyPressed

    private void prescription_1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_prescription_1MouseMoved

    }//GEN-LAST:event_prescription_1MouseMoved

    private void prescription_2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_2KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(prescription_1, 1);
                Utils.ChangeFont(prescription_2, 1);
                Utils.ChangeFont(prescription_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(prescription_1, -1);
                Utils.ChangeFont(prescription_2, -1);
                Utils.ChangeFont(prescription_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
        }
    }//GEN-LAST:event_prescription_2KeyPressed

    private void prescription_2MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_prescription_2MouseMoved

    }//GEN-LAST:event_prescription_2MouseMoved

    private void prescription_3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_3KeyPressed
        if (evt.isAltDown()) {
            if (evt.getKeyCode() == evt.VK_UP) {
                Utils.ChangeFont(prescription_1, 1);
                Utils.ChangeFont(prescription_2, 1);
                Utils.ChangeFont(prescription_3, 1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
            else
            if (evt.getKeyCode() == evt.VK_DOWN) {
                Utils.ChangeFont(prescription_1, -1);
                Utils.ChangeFont(prescription_2, -1);
                Utils.ChangeFont(prescription_3, -1);
                OpenRepForm.config.SetValue(Configuration.Key_PatientDiagnosisDialog_Prescription_1, diagnosis_1.getFont().getSize());
            }
        }        
    }//GEN-LAST:event_prescription_3KeyPressed

    private void prescription_3MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_prescription_3MouseMoved

    }//GEN-LAST:event_prescription_3MouseMoved

    private void diagnosis_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_1KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_RIGHT) prescription_1.grabFocus();
        if (diagnosis_1_changed) return;
        EvaluateDiagnosisChange(0, false);
    }//GEN-LAST:event_diagnosis_1KeyReleased

    private void diagnosis_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_2KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_RIGHT) prescription_2.grabFocus();
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_LEFT) prescription_1.grabFocus();
        if (diagnosis_2_changed) return;
        EvaluateDiagnosisChange(1, false);
    }//GEN-LAST:event_diagnosis_2KeyReleased

    private void prescription_2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_2KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_RIGHT) diagnosis_3.grabFocus();
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_LEFT) diagnosis_2.grabFocus();
        if (diagnosis_2_changed) return;
        EvaluateDiagnosisChange(1, false);
    }//GEN-LAST:event_prescription_2KeyReleased

    private void diagnosis_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_diagnosis_3KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_RIGHT) prescription_3.grabFocus();
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_LEFT) prescription_2.grabFocus();
        if (diagnosis_3_changed) return;
        EvaluateDiagnosisChange(2, false);
    }//GEN-LAST:event_diagnosis_3KeyReleased

    private void prescription_3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_3KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_LEFT) diagnosis_3.grabFocus();
        if (diagnosis_3_changed) return;
        EvaluateDiagnosisChange(2, false);
    }//GEN-LAST:event_prescription_3KeyReleased

    private void prescription_1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_prescription_1KeyReleased
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_RIGHT) diagnosis_2.grabFocus();
        if (evt.isAltDown() && evt.getKeyCode() == evt.VK_LEFT) diagnosis_1.grabFocus();
        if (diagnosis_1_changed) return;
        EvaluateDiagnosisChange(0, false);
    }//GEN-LAST:event_prescription_1KeyReleased

    private void add_repertorization_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_repertorization_1ActionPerformed
        EvaluateDiagnosisChange(0, true);
        SaveRepertorization(true, current_top_position);
        diagnosis_1_changed = true;
        UpdateDisplayRepertorizationButtons(current_top_position);
    }//GEN-LAST:event_add_repertorization_1ActionPerformed

    private void previous_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previous_buttonActionPerformed
        SaveData();
        if (current_top_position != 0) current_top_position--;
        UpdateContents(current_top_position);
    }//GEN-LAST:event_previous_buttonActionPerformed

    private void next_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next_buttonActionPerformed
        SaveData();
        if (current_top_position+1 < diagnoses.size() - 1) current_top_position++;
        UpdateContents(current_top_position);
    }//GEN-LAST:event_next_buttonActionPerformed

    private void display_repertorization_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_display_repertorization_1ActionPerformed
        if (current_top_position == -1 || current_top_position > diagnoses.size() - 1 || diagnoses.get(current_top_position).appendices == null || diagnoses.get(current_top_position).appendices.size() < 1) {
            JOptionPane.showMessageDialog(rootPane, "This diagnosis does not contain a repertorization.");
            return;
        }
        int result = JOptionPane.showConfirmDialog(rootPane, "Opening a repertorization will delete any current unsaved repertorizations in OpenRep. Do you want to continue?");
        if (result != JOptionPane.OK_OPTION || result != JOptionPane.YES_OPTION) return;
        OpenRepForm.SetCurrentFileName(diagnoses.get(current_top_position).appendices.get(0).filename);
        if (OpenRepForm.LoadRepertorization(diagnoses.get(current_top_position).appendices.get(0).filename)) {
            int rslt = JOptionPane.showConfirmDialog(rootPane, "The repertorization was opened in OpenRep. Do you wish to display it?");
            if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
                force_pms_quit = true;
                SaveData();
                WritePatientDiagnosis(patient_filename);
                this.setVisible(false);
            }
        }
    }//GEN-LAST:event_display_repertorization_1ActionPerformed

    private void add_repertorization_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_repertorization_2ActionPerformed
        EvaluateDiagnosisChange(1, true);
        SaveRepertorization(true, current_top_position+1);
        diagnosis_2_changed = true;
        UpdateDisplayRepertorizationButtons(current_top_position);
    }//GEN-LAST:event_add_repertorization_2ActionPerformed

    private void add_repertorization_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add_repertorization_3ActionPerformed
        EvaluateDiagnosisChange(2, true);
        SaveRepertorization(true, current_top_position+2);
        diagnosis_3_changed = true;
        UpdateDisplayRepertorizationButtons(current_top_position);
    }//GEN-LAST:event_add_repertorization_3ActionPerformed

    private void display_repertorization_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_display_repertorization_2ActionPerformed
        if (current_top_position+1 == -1 || current_top_position+1 > diagnoses.size() - 1 || diagnoses.get(current_top_position+1).appendices == null || diagnoses.get(current_top_position+1).appendices.size() < 1) {
            JOptionPane.showMessageDialog(rootPane, "This diagnosis does not contain a repertorization.");
            return;
        }
        int result = JOptionPane.showConfirmDialog(rootPane, "Opening a repertorization will delete any current unsaved repertorizations in OpenRep. Do you want to continue?");
        if (result != JOptionPane.OK_OPTION || result != JOptionPane.YES_OPTION) return;
        OpenRepForm.SetCurrentFileName(diagnoses.get(current_top_position+1).appendices.get(0).filename);
        if (OpenRepForm.LoadRepertorization(diagnoses.get(current_top_position+1).appendices.get(0).filename)) {
            int rslt = JOptionPane.showConfirmDialog(rootPane, "The repertorization was opened in OpenRep. Do you wish to display it?");
            if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
                force_pms_quit = true;
                SaveData();
                WritePatientDiagnosis(patient_filename);
                this.setVisible(false);
            }
        }

    }//GEN-LAST:event_display_repertorization_2ActionPerformed

    private void display_repertorization_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_display_repertorization_3ActionPerformed
        if (current_top_position+2 == -1 || current_top_position+2 > diagnoses.size() - 1 || diagnoses.get(current_top_position+2).appendices == null || diagnoses.get(current_top_position+2).appendices.size() < 1) {
            JOptionPane.showMessageDialog(rootPane, "This diagnosis does not contain a repertorization.");
            return;
        }
        int result = JOptionPane.showConfirmDialog(rootPane, "Opening a repertorization will delete any current unsaved repertorizations in OpenRep. Do you want to continue?");
        if (result != JOptionPane.OK_OPTION || result != JOptionPane.YES_OPTION) return;
        OpenRepForm.SetCurrentFileName(diagnoses.get(current_top_position+2).appendices.get(0).filename);
        if (OpenRepForm.LoadRepertorization(diagnoses.get(current_top_position+2).appendices.get(0).filename)) {
            int rslt = JOptionPane.showConfirmDialog(rootPane, "The repertorization was opened in OpenRep. Do you wish to display it?");
            if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
                force_pms_quit = true;
                SaveData();
                WritePatientDiagnosis(patient_filename);
                this.setVisible(false);
            }
        }
    }//GEN-LAST:event_display_repertorization_3ActionPerformed

    private void delete_diagnosis_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_diagnosis_1ActionPerformed
        int rslt = JOptionPane.showConfirmDialog(rootPane, "Do you really want to delete this diagnosis?");
        if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
            SaveData();
            diagnoses.remove(current_top_position);
            UpdateEdits(current_top_position);
            UpdateContents(current_top_position);
        }
    }//GEN-LAST:event_delete_diagnosis_1ActionPerformed

    private void delete_diagnosis_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_diagnosis_2ActionPerformed
        int rslt = JOptionPane.showConfirmDialog(rootPane, "Do you really want to delete this diagnosis?");
        if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
            SaveData();
            diagnoses.remove(current_top_position+1);
            UpdateEdits(current_top_position);
            UpdateContents(current_top_position);
        }

    }//GEN-LAST:event_delete_diagnosis_2ActionPerformed

    private void delete_diagnosis_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_diagnosis_3ActionPerformed
        int rslt = JOptionPane.showConfirmDialog(rootPane, "Do you really want to delete this diagnosis?");
        if (rslt == JOptionPane.YES_OPTION || rslt == JOptionPane.OK_OPTION) {
            SaveData();
            diagnoses.remove(current_top_position+2);
            UpdateEdits(current_top_position);
            UpdateContents(current_top_position);
        }
    }//GEN-LAST:event_delete_diagnosis_3ActionPerformed

    private void DateEditButton_1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DateEditButton_1ActionPerformed
        try {
            this.setVisible(false);
            String temps = JOptionPane.showInputDialog("Please enter date / time:", date_label_1.getText() + " " +time_label_1.getText());
            String[] date = temps.split(" ");
            if (date.length != 2) {
                JOptionPane.showMessageDialog(rootPane, "Please enter a valid date / time");
                return;
            }
            SaveData();
            diagnoses.get(current_top_position).date = temps;
            UpdateEdits(current_top_position);
            UpdateContents(current_top_position);
            Collections.sort(diagnoses);
            current_top_position = 0;
            UpdateContents(0);
            UpdateEdits(0);
        } finally {
            this.setVisible(true);
        }
    }//GEN-LAST:event_DateEditButton_1ActionPerformed

    private void DateEditButton_2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DateEditButton_2ActionPerformed
        try {
            this.setVisible(false);
            String temps = JOptionPane.showInputDialog("Please enter date / time:", date_label_2.getText() + " " +time_label_2.getText());
            String[] date = temps.split(" ");
            if (date.length != 2) {
                JOptionPane.showMessageDialog(rootPane, "Please enter a valid date / time");
                return;
            }
            diagnoses.get(current_top_position+1).date = temps;
            SaveData();
            UpdateEdits(current_top_position+1);
            UpdateContents(current_top_position+1);
            Collections.sort(diagnoses);
            current_top_position = 0;
            UpdateContents(0);
            UpdateEdits(0);
        } finally {
            this.setVisible(true);
        }

    }//GEN-LAST:event_DateEditButton_2ActionPerformed

    private void DateEditButton_3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DateEditButton_3ActionPerformed
        try {
            this.setVisible(false);
            String temps = JOptionPane.showInputDialog("Please enter date / time:", date_label_3.getText() + " " +time_label_3.getText());
            String[] date = temps.split(" ");
            if (date.length != 2) {
                JOptionPane.showMessageDialog(rootPane, "Please enter a valid date / time");
                return;
            }
            SaveData();
            diagnoses.get(current_top_position+2).date = temps;
            UpdateEdits(current_top_position+2);
            UpdateContents(current_top_position+2);
            Collections.sort(diagnoses);
            current_top_position = 0;
            UpdateContents(0);
            UpdateEdits(0);
        } finally {
            this.setVisible(true);
        }
    }//GEN-LAST:event_DateEditButton_3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton DateEditButton_1;
    private javax.swing.JButton DateEditButton_2;
    private javax.swing.JButton DateEditButton_3;
    private javax.swing.JLabel PatientLabel;
    private javax.swing.JButton add_repertorization_1;
    private javax.swing.JButton add_repertorization_2;
    private javax.swing.JButton add_repertorization_3;
    private javax.swing.JLabel date_label_1;
    private javax.swing.JLabel date_label_2;
    private javax.swing.JLabel date_label_3;
    private javax.swing.JButton delete_diagnosis_1;
    private javax.swing.JButton delete_diagnosis_2;
    private javax.swing.JButton delete_diagnosis_3;
    private javax.swing.JTextArea diagnosis_1;
    private javax.swing.JTextArea diagnosis_2;
    private javax.swing.JTextArea diagnosis_3;
    private javax.swing.JButton display_repertorization_1;
    private javax.swing.JButton display_repertorization_2;
    private javax.swing.JButton display_repertorization_3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JButton next_button;
    private javax.swing.JTextArea prescription_1;
    private javax.swing.JTextArea prescription_2;
    private javax.swing.JTextArea prescription_3;
    private javax.swing.JButton previous_button;
    private javax.swing.JLabel time_label_1;
    private javax.swing.JLabel time_label_2;
    private javax.swing.JLabel time_label_3;
    // End of variables declaration//GEN-END:variables

}
