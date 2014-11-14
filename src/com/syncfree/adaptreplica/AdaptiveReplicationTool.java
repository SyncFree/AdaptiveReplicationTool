package com.syncfree.adaptreplica;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aasco.gui.LogView;
import com.aasco.util.ArgDef;
import com.aasco.util.Args;
import com.aasco.util.PropertyDef;
import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.algorithm.IAlgorithm;
import com.syncfree.adaptreplica.controller.DataCentre;
import com.syncfree.adaptreplica.gui.OverallView;
import com.syncfree.adaptreplica.util.XmlHelper;

/**
 * Entry point to the tool.
 *
 * @author aas
 */
public class AdaptiveReplicationTool {
    // Defaults
    /** The default log level, valid values: ERROR, WARNING, INFO, DEBUG, ALL. */
    private static final String DEFAULT_LOG_LEVEL = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.log_level", Level.ALL.getName())
            .getProperty();
    /**
     * Definition of the command line arguments for the key 'm'. Format:
     * <p>
     * -m properties_filename
     * <p>
     * Input the property file used by this application.
     */
    public static final ArgDef KEY_PROPERTIES_FILENAME = ArgDef.getArgDef("m");
    /**
     * The customer properties filename. It has prevalence over the default one,
     * but it is only loaded if one has not been set in the command line.
     */
    private static final String CUSTOMER_PROPERTIES_FILENAME = "adpreplica.properties";
    /** The default properties filename. */
    private static final String DEFAULT_PROPERTIES_FILENAME = "adpreplica_defaults.properties";

    private static boolean msbPropertiesLoaded = false;

    /**
     * Starts the tool.
     * 
     * @param astrArgs
     *            .
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws IOException
     */
    public static void main(final String[] astrArgs)
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException {
        final LogView logView;

        setProperties(astrArgs);
        logView = setLogView(astrArgs);

        Verbose.finest("Entering main(String[])");

        // Set Look and Feel
        try {
            final String strLooAndFell = "Nimbus";
            boolean notFound = true;

            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (strLooAndFell.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    notFound = false;

                    break;
                }
            } // end for

            if (notFound) {
                if (strLooAndFell != null && strLooAndFell.length() > 0) {
                    Verbose.info(
                            "Look and Feel \"{0}\" id not installed; Using default system Look and Feel \"{1}\"",
                            strLooAndFell, UIManager
                            .getSystemLookAndFeelClassName());
                }
 
                // Take the menu bar off the JFrame
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                // Set the name of the application menu item
                System.setProperty(
                        "com.apple.mrj.application.apple.menu.about.name",
                        OverallView.TITLE);
                // Set the look and feel
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            Verbose.log(e, "Failed to set look and feel; {0}", e.getMessage());
        } // end try

        // Load DCs
        final Document dom = parseXmlFile("config.xml");
        final Element root = XmlHelper.getNode(dom.getChildNodes(), "config");
        final Element algArgs = XmlHelper.getNode(root.getChildNodes(),
                "default_alg");
        final NodeList dcs = XmlHelper.getNode(root.getChildNodes(), "dcs")
                .getChildNodes();
        final IAlgorithm alg;
        final OverallView view;

        alg = DataCentre.buildAlgorithm(algArgs);
        for (int i = 0; i < dcs.getLength(); ++i) {
            final Node node = dcs.item(i);

            if (node instanceof Element) {
                new DataCentre((Element) node, alg);
            }
        } // end for
        view = new OverallView(astrArgs, logView, DataCentre.getController(),
                root);
        DataCentre.getController().initialise(view);

        DataCentre.startAll();

        Verbose.info("Started!");
        Verbose.finest("Exiting main(String[])");
    } // main()

    /**
     * Creates the document representing and giving access to the specified XML
     * file.
     * 
     * @return the XML document representation.
     */
    private static Document parseXmlFile(String strXml) {
        // get the factory
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // Using factory get an instance of document builder
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document dom;
            File file;

            file = new File(strXml);
            if (!file.exists()) {
                strXml = "data/" + strXml;
            }

            // Parse using builder to get DOM representation of the XML file
            dom = db.parse(strXml);

            return dom;
        } catch (final Exception e) {
            Verbose.log(Level.SEVERE, e, "Laoding Specifications",
                    e.getMessage());
        } // end try

        return null;
    } // parseXmlFile()

    /**
     * Creates a GUI log view and registers the GUI log view to be notified of
     * any logged message.
     * 
     * @param astrArgs
     *            the command line arguments.
     * @return the GUI log view.
     */
    private static LogView setLogView(final String[] astrArgs) {
        final LogView logView = new LogView();
        Args args;

        try {
            args = ArgDef.ARGDEF_LOGGING_LEVEL.get(astrArgs);
            if (args == null && DEFAULT_LOG_LEVEL != null) {
                args = ArgDef.ARGDEF_LOGGING_LEVEL.get(new String[] {
                        '-' + ArgDef.ARGDEF_LOGGING_LEVEL.getKey(),
                        DEFAULT_LOG_LEVEL });
            }
        } catch (Exception me) {
            args = null;
        } // end try
        Verbose.set(args, astrArgs, logView);

        return logView;
    } // setLogView()

    /**
     * Sets the properties filename.
     * 
     * @param astrArgs
     *            the command line arguments.
     */
    private static synchronized void setProperties(final String[] astrArgs) {
        try {
            final String strPropertiesFilename;

            if (Args.exists(astrArgs,
                    AdaptiveReplicationTool.KEY_PROPERTIES_FILENAME)) {
                final Args args = AdaptiveReplicationTool.KEY_PROPERTIES_FILENAME
                        .get(astrArgs);

                strPropertiesFilename = args.getArg(0);
            } else {
                strPropertiesFilename = DEFAULT_PROPERTIES_FILENAME;
            }
            setProperties(strPropertiesFilename);
        } catch (Exception excp) {
            throw new RuntimeException(excp);
        } // end try
    } // setProperties()

    /**
     * Sets the specified properties filename.
     * 
     * @param strPropertiesFilename
     *            the properties filename.
     */
    private static synchronized void setProperties(
            final String strPropertiesFilename) {
        if (msbPropertiesLoaded) {
            Verbose.warning("Properties already loaded; ignoring request");

            return;
        }

        File oFile = new File(strPropertiesFilename);

        if (!oFile.exists()) {
            // Try to get it from jar file
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            final InputStream input;

            if (classLoader == null) {
                classLoader = Class.class.getClassLoader();
            }
            input = classLoader
                    .getResourceAsStream('/' + strPropertiesFilename);
            PropertyDef.setProperties(input, null);
        } else {
            PropertyDef.setProperties(new File(strPropertiesFilename), null,
                    false);
        }
        if (strPropertiesFilename == DEFAULT_PROPERTIES_FILENAME) {
            oFile = new File(CUSTOMER_PROPERTIES_FILENAME);
            if (oFile.exists() && oFile.isFile()) {
                PropertyDef.addProperties(oFile);
            }
        }
        AdaptiveReplicationTool.msbPropertiesLoaded = true;
    } // setProperties()
} // end class AdaptiveReplicationTool
