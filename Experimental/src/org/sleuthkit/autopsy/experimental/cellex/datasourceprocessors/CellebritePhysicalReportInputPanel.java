/*
 * Autopsy Forensic Browser
 *
 * Copyright 2015 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.experimental.cellex.datasourceprocessors;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessor;
import org.sleuthkit.autopsy.coreutils.ModuleSettings;
import org.sleuthkit.autopsy.coreutils.PathValidator;

public class CellebritePhysicalReportInputPanel extends JPanel implements DocumentListener {

    private static final long serialVersionUID = 1L;

    private final String PROP_LASTIMAGE_PATH = "LBL_LastImage_PATH";

    private PropertyChangeSupport pcs = null;
    private JFileChooser fc = new JFileChooser();

    // Externally supplied name is used to store settings 
    private String contextName;

    /**
     * Creates new form CellebriteAndroidInputPanel
     */
    public CellebritePhysicalReportInputPanel(String context) {
        initComponents();

        errorLabel.setVisible(false);

        fc.setDragEnabled(false);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        this.contextName = context;
        pcs = new PropertyChangeSupport(this);

        createTimeZoneList();
    }

    /**
     * Creates and returns an instance of a ImageFilePanel.
     */
    public static synchronized CellebritePhysicalReportInputPanel createInstance(String context) {
        CellebritePhysicalReportInputPanel instance = new CellebritePhysicalReportInputPanel(context);
        instance.postInit();

        return instance;
    }

    //post-constructor initialization to properly initialize listener support
    //without leaking references of uninitialized objects
    private void postInit() {
        pathTextField.getDocument().addDocumentListener(this);
    }

    /**
     * Creates the drop down list for the time zones and then makes the local
     * machine time zone to be selected.
     */
    public void createTimeZoneList() {
        // load and add all timezone
        String[] ids = SimpleTimeZone.getAvailableIDs();
        for (String id : ids) {
            TimeZone zone = TimeZone.getTimeZone(id);
            int offset = zone.getRawOffset() / 1000;
            int hour = offset / 3600;
            int minutes = (offset % 3600) / 60;
            String item = String.format("(GMT%+d:%02d) %s", hour, minutes, id);

            /*
             * DateFormat dfm = new SimpleDateFormat("z");
             * dfm.setTimeZone(zone); boolean hasDaylight =
             * zone.useDaylightTime(); String first = dfm.format(new Date(2010,
             * 1, 1)); String second = dfm.format(new Date(2011, 6, 6)); int mid
             * = hour * -1; String result = first + Integer.toString(mid);
             * if(hasDaylight){ result = result + second; }
             * timeZoneComboBox.addItem(item + " (" + result + ")");
             */
            timeZoneComboBox.addItem(item);
        }
        // get the current timezone
        TimeZone thisTimeZone = Calendar.getInstance().getTimeZone();
        int thisOffset = thisTimeZone.getRawOffset() / 1000;
        int thisHour = thisOffset / 3600;
        int thisMinutes = (thisOffset % 3600) / 60;
        String formatted = String.format("(GMT%+d:%02d) %s", thisHour, thisMinutes, thisTimeZone.getID());

        // set the selected timezone
        timeZoneComboBox.setSelectedItem(formatted);
    }

    /**
     * Get the path of the user selected folder.
     *
     * @return the image path
     */
    public String getContentPaths() {
        return pathTextField.getText();
    }

    /**
     * Set the path of the images folder.
     */
    public void setContentPath(String s) {
        pathTextField.setText(s);
    }

    public String getTimeZone() {
        String tz = timeZoneComboBox.getSelectedItem().toString();
        return tz.substring(tz.indexOf(")") + 2).trim();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pathLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        timeZoneLabel = new javax.swing.JLabel();
        timeZoneComboBox = new javax.swing.JComboBox<>();
        errorLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(pathLabel, org.openide.util.NbBundle.getMessage(CellebritePhysicalReportInputPanel.class, "CellebritePhysicalReportInputPanel.pathLabel.text")); // NOI18N

        pathTextField.setText(org.openide.util.NbBundle.getMessage(CellebritePhysicalReportInputPanel.class, "CellebritePhysicalReportInputPanel.pathTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(CellebritePhysicalReportInputPanel.class, "CellebritePhysicalReportInputPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(timeZoneLabel, org.openide.util.NbBundle.getMessage(CellebritePhysicalReportInputPanel.class, "CellebritePhysicalReportInputPanel.timeZoneLabel.text")); // NOI18N

        timeZoneComboBox.setMaximumRowCount(30);

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(CellebritePhysicalReportInputPanel.class, "CellebritePhysicalReportInputPanel.errorLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pathLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(timeZoneLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(errorLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pathLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addGap(8, 8, 8)
                .addComponent(errorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeZoneLabel)
                    .addComponent(timeZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(198, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    @SuppressWarnings("deprecation")
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
      String oldText = pathTextField.getText();

      // set the current directory of the FileChooser if the ImagePath Field is valid
      File currentDir = new File(oldText);
      if (currentDir.exists()) {
          fc.setCurrentDirectory(currentDir);
      }

      int retval = fc.showOpenDialog(this);
      if (retval == JFileChooser.APPROVE_OPTION) {
          String path = fc.getSelectedFile().getPath();
          pathTextField.setText(path);
      }

      pcs.firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.FOCUS_NEXT.toString(), false, true);
    }//GEN-LAST:event_browseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JComboBox<String> timeZoneComboBox;
    private javax.swing.JLabel timeZoneLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Update functions are called by the pathTextField which has this set as
     * it's DocumentEventListener. Each update function fires a property change
     * to be caught by the parent panel.
     *
     * @param e the event, which is ignored
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        pcs.firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        pcs.firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        pcs.firePropertyChange(DataSourceProcessor.DSP_PANEL_EVENT.UPDATE_UI.toString(), false, true);
    }

    /**
     * Set the focus to the pathTextField.
     */
    public void select() {
        pathTextField.requestFocusInWindow();
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        super.addPropertyChangeListener(pcl);

        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        pcs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        super.removePropertyChangeListener(pcl);

        pcs.removePropertyChangeListener(pcl);
    }

    /**
     * Should we enable the next button of the wizard?
     *
     * @return true if a proper image has been selected, false otherwise
     */
    public boolean validatePanel() {
        errorLabel.setVisible(false);
        String path = getContentPaths();
        if (path == null || path.isEmpty()) {
            return false;
        }

        // display warning if there is one (but don't disable "next" button)
        warnIfPathIsInvalid(path);

        // check if a folder exists by this name.
        File f = new File(path);
        boolean isValid = (f.exists() && f.isDirectory());

        return isValid;
    }

    /**
     * Validates path to selected data source and displays warning if it is
     * invalid.
     *
     * @param path Absolute path to the selected data source
     */
    private void warnIfPathIsInvalid(String path) {
        if (!PathValidator.isValid(path, Case.getCurrentCase().getCaseType())) {
            errorLabel.setVisible(true);
            errorLabel.setText("Path to multi-user data source is on \"C:\" drive");
        }
    }

    public void reset() {
        //reset the UI elements to default 
        pathTextField.setText(null);
    }

    /*
     * Store the last used settings
     */
    public void storeSettings() {
        String imagePathName = getContentPaths();
        if (null != imagePathName) {
            String imagePath = imagePathName.substring(0, imagePathName.lastIndexOf(File.separator) + 1);
            ModuleSettings.setConfigSetting(contextName, PROP_LASTIMAGE_PATH, imagePath);
        }
    }

    /*
     * Read and load the last used settings
     */
    public void readSettings() {
        String lastImagePath = ModuleSettings.getConfigSetting(contextName, PROP_LASTIMAGE_PATH);
        if (null != lastImagePath) {
            if (!lastImagePath.isEmpty()) {
                pathTextField.setText(lastImagePath);
            }
        }
    }

}
