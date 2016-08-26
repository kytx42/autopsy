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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.sleuthkit.autopsy.casemodule.GeneralFilter;
import org.sleuthkit.autopsy.corecomponentinterfaces.AutomatedIngestDataSourceProcessor;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorProgressMonitor;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorCallback;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessor;
import org.w3c.dom.Document;

/**
 * A Cellebrite XML report file data source processor that implements the
 * DataSourceProcessor service provider interface to allow integration with the
 * add data source wizard. It also provides a run method overload to allow it to
 * be used independently of the wizard.
 */
@ServiceProviders(value={
    @ServiceProvider(service=DataSourceProcessor.class),
    @ServiceProvider(service=AutomatedIngestDataSourceProcessor.class)}
)
public class CellebriteLogicalReportProcessor implements AutomatedIngestDataSourceProcessor {

    private static final String DATA_SOURCE_TYPE = "Cellebrite XML";
    private static final List<String> CELLEBRITE_EXTS = Arrays.asList(new String[]{".xml"});
    private static final String CELLEBRITE_DESC = "Cellebrite XML Files (*.xml)";
    private static final GeneralFilter xmlFilter = new GeneralFilter(CELLEBRITE_EXTS, CELLEBRITE_DESC);
    private static final List<FileFilter> cellebriteLogicalReportFilters = new ArrayList<>();
    private final CellebriteLogicalReportPanel configPanel;
    private AddCellebriteLogicalReportTask addCellebriteXMLTask;
    static {
        cellebriteLogicalReportFilters.add(xmlFilter);
    }

    private enum ReportType {
        CELLEBRITE_LOGICAL_HANDSET,
        CELLEBRITE_LOGICAL_SIM,
        INVALID_REPORT,
    }

    /**
     * Gets the file extensions supported by this data source processor as a
     * list of file filters.
     *
     * @return List<FileFilter> List of FileFilter objects
     */
    public static final List<FileFilter> getFileFilterList() {
        return cellebriteLogicalReportFilters;
    }

    /*
     * Constructs a Cellebrite XML report file data source processor that
     * implements the DataSourceProcessor service provider interface to allow
     * integration with the add data source wizard. It also provides a run
     * method overload to allow it to be used independently of the wizard.
     */
    public CellebriteLogicalReportProcessor() {
        configPanel = CellebriteLogicalReportPanel.createInstance(CellebriteLogicalReportProcessor.class.getName(), cellebriteLogicalReportFilters);
    }

    /**
     * Gets a string that describes the type of data sources this processor is
     * able to add to the case database. The string is suitable for display in a
     * type selection UI component (e.g., a combo box).
     *
     * @return A data source type display string for this data source processor.
     */
    @Override
    public String getDataSourceType() {
        return DATA_SOURCE_TYPE;
    }

    /**
     * Gets the panel that allows a user to select a data source and do any
     * configuration required by the data source. The panel is less than 544
     * pixels wide and less than 173 pixels high.
     *
     * @return A selection and configuration panel for this data source
     *         processor.
     */
    @Override
    public JPanel getPanel() {
        configPanel.readSettings();
        configPanel.select();
        return configPanel;
    }

    /**
     * Indicates whether the settings in the selection and configuration panel
     * are valid and complete.
     *
     * @return True if the settings are valid and complete and the processor is
     *         ready to have its run method called, false otherwise.
     */
    @Override
    public boolean isPanelValid() {
        return configPanel.validatePanel();
    }

    /**
     * Adds a data source to the case database using a background task in a
     * separate thread and the settings provided by the selection and
     * configuration panel. Returns as soon as the background task is started.
     * The background task uses a callback object to signal task completion and
     * return results.
     *
     * This method should not be called unless isPanelValid returns true.
     *
     * @param progressMonitor Progress monitor that will be used by the
     *                        background task to report progress.
     * @param callback        Callback that will be used by the background task
     *                        to return results.
     */
    @Override
    public void run(DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callback) {
        configPanel.storeSettings();
        String deviceId = UUID.randomUUID().toString();
        run(deviceId, deviceId, configPanel.getImageFilePath(), configPanel.isHandsetFile(), progressMonitor, callback);
    }

    /**
     * Adds a data source to the case database using a background task in a
     * separate thread and the given settings instead of those provided by the
     * selection and configuration panel. Returns as soon as the background task
     * is started and uses the callback object to signal task completion and
     * return results.
     *
     * @param deviceId                 An ASCII-printable identifier for the
     *                                 device associated with the data source
     *                                 that is intended to be unique across
     *                                 multiple cases (e.g., a UUID).
     * @param rootVirtualDirectoryName The name to give to the virtual directory
     *                                 that will represent the data source. Pass
     *                                 the empty string to get a default name of
     *                                 the form: LogicalFileSet[N]
     * @param cellebriteXmlFilePath    Path to a Cellebrite report XML file.
     * @param isHandsetFile            Indicates whether the XML file is for a
     *                                 handset or a SIM.
     * @param progressMonitor          Progress monitor for reporting progress
     *                                 during processing.
     * @param callback                 Callback to call when processing is done.
     */
    public void run(String deviceId, String rootVirtualDirectoryName, String cellebriteXmlFilePath, boolean isHandsetFile, DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callback) {
        AddCellebriteLogicalReportTask.CellebriteInputType inputType;
        if (isHandsetFile) {
            inputType = AddCellebriteLogicalReportTask.CellebriteInputType.handset;
        } else {
            inputType = AddCellebriteLogicalReportTask.CellebriteInputType.SIM;
        }
        addCellebriteXMLTask = new AddCellebriteLogicalReportTask(deviceId, rootVirtualDirectoryName, cellebriteXmlFilePath, inputType, progressMonitor, callback);
        new Thread(addCellebriteXMLTask).start();
    }

    /**
     * Requests cancellation of the background task that adds a data source to
     * the case database, after the task is started using the run method. This
     * is a "best effort" cancellation, with no guarantees that the case
     * database will be unchanged. If cancellation succeeded, the list of new
     * data sources returned by the background task will be empty.
     */
    @Override
    public void cancel() {
        addCellebriteXMLTask.cancelTask();
    }

    /**
     * Resets the selection and configuration panel for this data source
     * processor.
     */
    @Override
    public void reset() {
        configPanel.reset();
    }
    
    /**
     * Attempts to parse a data source as a Cellebrite logical report.
     *
     * @param dataSourcePath The path to the data source.
     *
     * @return Type of Cellebrite logical report if the data source is a valid
     *         Cellebrite logical report file, null otherwise.
     */
    private ReportType parseCellebriteLogicalReportType(Path dataSourcePath) {

        if (!isAcceptedByFiler(dataSourcePath.toFile(), cellebriteLogicalReportFilters)) {
            return ReportType.INVALID_REPORT;
        }

        String report_type;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(dataSourcePath.toFile());
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/reports/report/general_information/report_type/text()");
            report_type = (String) expr.evaluate(doc, XPathConstants.STRING);
            if (report_type.equalsIgnoreCase("sim")) {
                return ReportType.CELLEBRITE_LOGICAL_SIM;
            } else if (report_type.equalsIgnoreCase("cell")) {
                return ReportType.CELLEBRITE_LOGICAL_HANDSET;
            } else {
                return ReportType.INVALID_REPORT;
            }
        } catch (Exception ignore) {
            // Not a valid Cellebrite logical report file.
            return ReportType.INVALID_REPORT;
        }
    }
    
    private static boolean isAcceptedByFiler(File file, List<FileFilter> filters) {
        for (FileFilter filter : filters) {
            if (filter.accept(file)) {
                return true;
            }
        }
        return false;
    }       

    @Override
    public int canProcess(Path dataSourcePath) throws AutomatedIngestDataSourceProcessorException {
        ReportType type = parseCellebriteLogicalReportType(dataSourcePath);
        switch (type) {
            case CELLEBRITE_LOGICAL_HANDSET:
            case CELLEBRITE_LOGICAL_SIM:
                return 100;
            case INVALID_REPORT:
            default:
                return 0;
        }
    }

    @Override
    public void process(String deviceId, Path dataSourcePath, DataSourceProcessorProgressMonitor progressMonitor, DataSourceProcessorCallback callBack) throws AutomatedIngestDataSourceProcessorException {
        ReportType type = parseCellebriteLogicalReportType(dataSourcePath);
        boolean isHandsetFile = false;
        switch (type) {
            case CELLEBRITE_LOGICAL_HANDSET:
                isHandsetFile = true;
                break;
            case CELLEBRITE_LOGICAL_SIM:
                isHandsetFile = false;
                break;
            case INVALID_REPORT:
            default:
                // ELTODO : should we attempt to process XML reports even though we couldn't identify report type?
                break;
        } 
        run(deviceId, deviceId, dataSourcePath.toString(), isHandsetFile, progressMonitor, callBack);
    }
}
