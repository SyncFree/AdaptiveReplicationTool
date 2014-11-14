package com.syncfree.adaptreplica.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aasco.gui.AboutDlg;
import com.aasco.gui.HelpDlg;
import com.aasco.gui.LogGUIView;
import com.aasco.gui.LogView;
import com.aasco.util.ArgDef;
import com.aasco.util.ImageManager;
import com.aasco.util.ParamDef;
import com.aasco.util.PropertyDef;
import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.algorithm.IAlgorithmListener;
import com.syncfree.adaptreplica.controller.DataCentre;
import com.syncfree.adaptreplica.controller.IController;
import com.syncfree.adaptreplica.controller.IFullController;
import com.syncfree.adaptreplica.controller.IMessageListener;
import com.syncfree.adaptreplica.controller.IProcessor;
import com.syncfree.adaptreplica.controller.IResetable;
import com.syncfree.adaptreplica.controller.Message;
import com.syncfree.adaptreplica.controller.Message.TYPE;
import com.syncfree.adaptreplica.controller.Processor;
import com.syncfree.adaptreplica.controller.ReadRequest;
import com.syncfree.adaptreplica.controller.Response;
import com.syncfree.adaptreplica.controller.WriteRequest;
import com.syncfree.adaptreplica.test.ITestCtrler;
import com.syncfree.adaptreplica.test.TestesController;
import com.syncfree.adaptreplica.util.XmlHelper;

/**
 * Display of the DCs emulated by this computer.
 * 
 * @author aas
 */
public class OverallView extends JFrame implements WindowListener, Action,
        IProcessor, IMessageListener, IAlgorithmListener, IResetable {
    /**
     * A version number for this class so that serialisation can occur without
     * worrying about the underlying class changing between serialisation and
     * deserialisation.
     * <p>
     *
     * Not that we ever serialise this class of course, but JFrame implements
     * Serializable, so therefore by default we do as well.
     */
    private static final long serialVersionUID = 7144589205689323083L;

    private static final String LISTENER_GUI = DataCentre.LISTENER_GUI;

    // XML element names
    private static final String NODE_DCS = "dcs";
    private static final String NODE_TESTS = "tests";
    private static final String NODE_GUI = "gui";

    /**
     * Definition of the command line arguments for the key 'm'. Format:
     * <p>
     * -m properties_filename
     * <p>
     * Input the property file used by this application.
     */
    public static final ArgDef KEY_PROPERTIES_FILENAME = new ArgDef("m", false,
            false, "Set the property file to be used", ParamDef.stringInstance(
                    "properties_filename", true,
                    "The name of the property file"));

    public static final String DEFAULT_TITLE = "Adaptive Replication Tool";
    public static final String TITLE = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.title", DEFAULT_TITLE).getProperty();

    // Application dimensions
    /** The width of the dialog. */
    private static final PropertyDef<Integer> WIDTH = PropertyDef
            .getPropertyDef(
                    "com.syncfree.adaptreplica.gui.solution_dialog_width", 856);
    /** The height of the window. */
    private static final PropertyDef<Integer> HEIGHT = PropertyDef
            .getPropertyDef(
                    "com.syncfree.adaptreplica.gui.solution_dialog_height", 400);
    /** The minimum width of the left pane. */
    private static final PropertyDef<Integer> HORIZONTAL_DIVIDER_LOCATION = PropertyDef
            .getPropertyDef(
                    "com.syncfree.adaptreplica1.gui.solution_horizontal_devider_loc",
                    240);

    // Menu
    private static final String MENU_FILE = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_file", "File").getProperty();
    private static final String MENU_VIEW = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_view", "View").getProperty();
    private static final String MENU_HELP = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_help", "Help").getProperty();
    private static final String MENU_QUIT = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_quit", "Quit").getProperty();
    private static final String MENU_LOG_VIEW = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_log_view", "Log View")
            .getProperty();

    private static final String MENU_HELP_ABOUT = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_help_about", "About")
            .getProperty();
    private static final String MENU_HELP_CONTENT = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.menu_help_content", "Content")
            .getProperty();

    private static final String DEFAULT_IMG_EXTENSION = ".png";

    // About
    private static final String ABOUT_NAME = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.about_name", "About: " + TITLE)
            .getProperty();
    private static final String ABOUT_IMG = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.about_img", (String) null)
            .getProperty();
    private static final String ABOUT_TOOLTIP = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.about_tooltip", "About")
            .getProperty();
    private static final String ABOUT_COPYRIGHT = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.about_copyright",
            "Copyright SyncFree 2014").getProperty();
    private static final String ABOUT_INFO = PropertyDef
            .getPropertyDef("com.syncfree.adaptreplica.gui.about_info",
                    "SyncFree project is an European project supported by the EU...\n")
            .getProperty();

    // Help
    private static final String[] CONTENT_NAMES = { DEFAULT_TITLE,
            "Introduction", "GUI Overview", "Future Work" };
    private static final String[] CONTENT_FILENAMES = {
            "data/help/adpReplicaTool.html", "data/help/introduction.html",
            "data/help/guiOverview.html", "data/help/futureWork.html" };
    private static final String CONTENT_TITLE = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.content_title", "Help: " + TITLE)
            .getProperty();
    private static final String CONTENT_IMG = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.content_img", (String) null)
            .getProperty();

    private static final long KEY_STROKE_DELAY = PropertyDef.getPropertyDef(
            "com.syncfree.adaptreplica.gui.delay_millisec", 500l).getProperty();

    private static final String ID_FONT_NAME = "Serif";
    private static final int ID_FONT_STYLE = Font.BOLD;
    private static final int ID_FONT_SIZE = 13;
    private static final String VALUE_FONT_NAME = "Serif";
    private static final int VALUE_FONT_STYLE = Font.PLAIN;
    private static final int VALUE_FONT_SIZE = 12;
    private static final int IMG_HEIGHT = 64; // pixels

    private static final String ACTION_MENU_HELP_ABOUT = "act_menu_help_about";
    private static final String ACTION_MENU_HELP_CONTENT = "act_menu_help_content";

    private static final char READ_PREFIX = 'r';
    private static final char WRITE_PREFIX = 'w';

    /** Cache the images to speed drawing process. */
    private final Map<Integer, BufferedImage> mImages = new HashMap<>();

    private final LogGUIView mLogView;
    private final JSplitPane mRightPane;
    private int mDividerLocation;
    private JCheckBoxMenuItem mLogViewMenuItem;

    private final IController mCtrl;
    private final Processor mMsgProcessor;
    private final List<DCData> mDCs;
    private boolean mbListening;
    private boolean mbStarted;
    private TestesController mTests;
    private final PlayerView mPlayerView;
    private final MyPanel mView;

    //
    private final String mstrIdFontName;
    private final int miIdFontStyle;
    private final int miIdFontSize;
    private final String mstrValueFontName;
    private final int miValueFontStyle;
    private final int miValueFontSize;
    private final int miImgHeight; // pixels

    public OverallView(final String[] astrArgs, final LogView logView,
            final IController ctrl, final Element node) throws IOException {
        super(TITLE);

        final NodeList dcs = XmlHelper.getNode(node, NODE_DCS).getChildNodes();
        final Element guiElement = XmlHelper.getNode(node, NODE_GUI);
        final int width = XmlHelper.getNodeValue(guiElement, "width",
                WIDTH.getProperty());
        final int height = XmlHelper.getNodeValue(guiElement, "height",
                HEIGHT.getProperty());

        this.mCtrl = ctrl;
        this.mMsgProcessor = new Processor(this);
        this.mbListening = false;

        //
        final MyListener listener = new MyListener();
        final JPanel topPanel = new JPanel(new BorderLayout());
        Element elem;
        String str;

        elem = XmlHelper.getNode(guiElement, "dc_id");
        this.mstrIdFontName = XmlHelper.getArg(elem, "font", ID_FONT_NAME);
        this.miIdFontStyle = XmlHelper.getArg(elem, "style", ID_FONT_STYLE);
        this.miIdFontSize = XmlHelper.getArg(elem, "size", ID_FONT_SIZE);
        elem = XmlHelper.getNode(guiElement, "dc_value");
        this.mstrValueFontName = XmlHelper
                .getArg(elem, "font", VALUE_FONT_NAME);
        this.miValueFontStyle = XmlHelper.getArg(elem, "style",
                VALUE_FONT_STYLE);
        this.miValueFontSize = XmlHelper.getArg(elem, "size", VALUE_FONT_SIZE);

        elem = XmlHelper.getNode(guiElement, "dc_img");
        this.miImgHeight = XmlHelper.getArg(elem, "height", IMG_HEIGHT);

        buildMenu(astrArgs, listener);

        str = XmlHelper.getNodeValue(guiElement, "background_img", null);
        this.mView = new MyPanel(width,
                (str == null || str.length() == 0) ? null : getImage(str));
        topPanel.add(this.mView, BorderLayout.CENTER);
        this.mPlayerView = new PlayerView(XmlHelper.getNode(node, NODE_TESTS)
                .getAttribute("start"));
        topPanel.add(this.mPlayerView, BorderLayout.SOUTH);

        this.mLogView = buildLogView(astrArgs, guiElement, listener, logView);
        this.mRightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel,
                this.mLogView);
        this.mRightPane.setOneTouchExpandable(true);
        this.mRightPane.setDividerLocation(XmlHelper.getNodeValue(guiElement,
                "devider_loc", HORIZONTAL_DIVIDER_LOCATION.getProperty()));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(this.mRightPane, BorderLayout.CENTER);
        //

        this.mDCs = new ArrayList<DCData>(dcs.getLength());
        for (int i = 0; i < dcs.getLength(); ++i) {
            final Node n = dcs.item(i);

            if (n instanceof Element) {
                final Element gui = XmlHelper.getNode(n.getChildNodes(),
                        NODE_GUI);
                final DCData data = new DCData(gui);

                this.mDCs.add(data);
            }
        } // end for

        setMinimumSize(new Dimension(width, height));
        pack();
        setSize(width, height);

        // Centre on screen
        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (int) ((d.getWidth() - getWidth()) / 2);
        final int y = (int) ((d.getHeight() - getHeight()) / 2);

        setLocation(x, y);
        setEnabled(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(listener);

        ((IFullController) this.mCtrl).register(this);

        // Tests
        final Node n = XmlHelper.getNode(node, NODE_TESTS);

        if (n != null) {
            final Element tests = (Element) n;

            str = tests.getAttribute("active").toUpperCase();
            if (str != null && Boolean.parseBoolean(str)) {
                this.mTests = new TestesController(this.mCtrl,
                        this.mPlayerView, tests);
                this.mCtrl.setTestsCtrler(this.mTests.getTestCtrler());
                this.mPlayerView.initilise(this.mTests);
            }
        }

        setIconImage();

        Verbose.finer("Leaving OverallView(Sring[])");
    } // Constructor ()

    @Override
    public void onChange(final DataCentre dc, final int iLevel) {
        for (final DCData d : this.mDCs) {
            if (d.getID().equals(dc.getID())) {
                final boolean bShowMsg = (d.getLevel() != iLevel);

                d.setLevel(iLevel);
                if (this.mbStarted) {
                    this.mView.repaint();
                    if (bShowMsg) {
                        Verbose.debug("New level {0} for {1}", iLevel,
                                d.getID());
                    }
                }

                break;
            }
        } // end for
    } // onChange()

    /**
     * Builds and writes the help into the log devices.
     */
    public static void help() {
        Verbose.log(Level.OFF, "-----------------------------------");
        Verbose.log(Level.OFF, "Help");
        Verbose.log(Level.OFF, "====");

        helpHeader();
        Verbose.log(Level.OFF, "\n");

        helpBody();
        Verbose.log(Level.OFF, "-----------------------------------");
    } // help()

    @Override
    public String getID() {
        return LISTENER_GUI;
    } // getID()

    @Override
    public void send(final Message<?> message) {
        this.mMsgProcessor.send(message);
    } // send()

    @Override
    public boolean process(final Message<?> msg) {
        final Response<?> response = (Response<?>) msg;

        switch (response.getRequestType()) {
        case READ:
            Verbose.debug("Read data from {0} requested by {1}",
                    msg.getSenderID(), getID());
            break;

        case WRITE:
            Verbose.debug("Wrote data from {0} to {1}", getID(),
                    msg.getSenderID());
            break;

        default:
            Verbose.warning(
                    "Invalid message: Message of type {0} was received from {1}",
                    msg.getType(), msg.getSenderID());
        } // end switch

        return false;
    } // process()

    @Override
    public synchronized void start() {
        if (this.mbListening) {
            Verbose.warning(
                    "{0} already running; ignoring request to start {0}",
                    getID());
        } else {
            this.mbStarted = true;
            this.mbListening = true;
            this.mMsgProcessor.start();
            setVisible(true);
        }
    } // start()

    @Override
    public void stop() {
        if (this.mbListening) {
            this.mTests.stop();
            this.mbListening = false;
            setVisible(false);
            this.mbStarted = false;
            this.mCtrl.sendAll(new Message<>(getID(), this.mMsgProcessor
                    .getNextMsgID(), TYPE.STOP, null));
        } else {
            Verbose.warning(
                    "{0} already stopped; ignoring request to stop {0}",
                    getID());
        }
    } // stop()

    @Override
    public void onStarting() {
        setVisible(true);
    } // onStarting()

    @Override
    public void onStopping() {
        setVisible(false);
    } // onStopping()

    // WindowListener implementation
    @Override
    public void windowOpened(final WindowEvent e) {
    } // windowOpened()

    @Override
    public void windowClosing(final WindowEvent e) {
        closeWindow();
    } // windowClosing()

    @Override
    public void windowClosed(final WindowEvent e) {
    } // windowClosed()

    @Override
    public void windowIconified(final WindowEvent e) {
    } // windowIconified()

    @Override
    public void windowDeiconified(final WindowEvent e) {
    } // windowDeiconified()

    @Override
    public void windowActivated(final WindowEvent e) {
    } // windowActivated()

    @Override
    public void windowDeactivated(final WindowEvent e) {
    } // windowDeactivated()
      // end WindowListener implementation

    /**
     * Called when exiting the application. This will request the closure of all
     * the DC and the processor for the GUI too.
     */
    public void closeWindow() {
        this.mCtrl.sendAll(new Message<>(getID(), this.mMsgProcessor
                .getNextMsgID(), TYPE.STOP, null));
    } // closeWindow()

    // Action interface
    @Override
    public void actionPerformed(final ActionEvent ae) {
        if (this.mbListening) {
            final String strAction = ae.getActionCommand();

            if (strAction.length() > 2 && strAction.charAt(1) == '_') {
                final String strID = strAction.substring(2);

                switch (strAction.charAt(0)) {
                case READ_PREFIX:
                    this.mCtrl.send(strID, new Message<>(getID(),
                            this.mMsgProcessor.getNextMsgID(), TYPE.READ, ""));
                    break;

                case WRITE_PREFIX:
                    this.mCtrl.send(strID, new Message<>(getID(),
                            this.mMsgProcessor.getNextMsgID(), TYPE.WRITE,
                            strID + ": " + this.mMsgProcessor.getMsgID()));
                    break;

                default:
                } // end switch
            }
        }
    } // actionPerformed()

    @Override
    public Object getValue(final String key) {
        return null;
    } // getValue()

    @Override
    public void putValue(final String key, final Object value) {
    } // putValue()
      // End Action interface

    @Override
    public boolean reset(final IResetable initiator) {
        this.mView.repaint();

        return true;
    } // reset()

    @Override
    public void setTestsCtrler(final ITestCtrler testCtrler) {
    } // setTestsCtrler()

    @Override
    public String toString() {
        return getID();
    } // toString()

    /**
     * Sets the default icon for the GUI.
     */
    private void setIconImage() {
        final String strImgName = "images/logo.png";
        final ImageIcon icon = ImageManager.buildImage(strImgName);
        
        setIconImage(icon.getImage());
    } // setIconImage()

    /**
     * Builds the header for the help.
     */
    private static void helpHeader() {
        final StringBuilder buf = new StringBuilder();

        buf.append("adptReplica");
        buf.append(" ");
        ArgDef.ARGDEF_LOGGING_LEVEL.helpHeader(buf);
        buf.append(" [");
        KEY_PROPERTIES_FILENAME.helpHeader(buf);
        buf.append("]");

        Verbose.log(Level.OFF, buf.toString());
    } // helpHeader()

    /**
     * Builds the body of the help.
     */
    private static void helpBody() {
        final StringBuilder buf = new StringBuilder();

        ArgDef.ARGDEF_LOGGING_LEVEL.helpBody(buf);
        KEY_PROPERTIES_FILENAME.helpBody(buf);

        Verbose.log(Level.OFF, buf.toString());
    } // helpBody()

    /**
     * Builds the full GUI log view.
     * 
     * @param astrArgs
     *            the command line arguments.
     * @param listener
     *            the action listener.
     * @param logView
     *            the GUI log view.
     * @return the built log view.
     */
    private LogGUIView buildLogView(final String[] astrArgs,
            final Element guiElement, final ActionListener listener,
            LogView logView) {
        final String str = XmlHelper.getNodeValue(guiElement, "log_view_level",
                null);
        final LogGUIView logGuiView;

        if (str != null) {
            logGuiView = new LogGUIView(logView, Level.parse(str.toUpperCase()));
        } else {
            logGuiView = new LogGUIView(logView);
        }
        logGuiView.setActionListener(listener);

        return logGuiView;
    } // buildLogView()

    /**
     * Builds and adds the menu to the GUI.
     * 
     * @param astrArgs
     *            the command line arguments.
     */
    private void buildMenu(final String[] astrArgs,
            final ActionListener listener) {
        Verbose.finest("Entering buildMenu(String[])");

        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu(MENU_FILE);
        final JMenu viewMenu = new JMenu(MENU_VIEW);
        final JMenu helpMenu = new JMenu(MENU_HELP);
        final JMenuItem quitMenuItem = new JMenuItem(MENU_QUIT);
        JMenuItem item;

        this.mLogViewMenuItem = new JCheckBoxMenuItem(MENU_LOG_VIEW);
        quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                Verbose.info("Exited!");

                // Quits the application when invoked.
                System.exit(0);
            } // actionPerformed()
        });
        quitMenuItem.setMnemonic(KeyEvent.VK_Q);
        fileMenu.add(quitMenuItem);
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        this.mLogViewMenuItem.setSelected(true);
        this.mLogViewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                final JCheckBoxMenuItem logViewMenuItem = (JCheckBoxMenuItem) ae
                        .getSource();

                if (logViewMenuItem.isSelected()) {
                    showLogView();
                } else {
                    hideLogView();
                }
            } // actionPerformed()
        });
        viewMenu.add(this.mLogViewMenuItem);
        menuBar.add(viewMenu);

        item = new JMenuItem(MENU_HELP_ABOUT);
        item.setActionCommand(ACTION_MENU_HELP_ABOUT);
        item.addActionListener(listener);
        helpMenu.add(item);
        item = new JMenuItem(MENU_HELP_CONTENT);
        item.setActionCommand(ACTION_MENU_HELP_CONTENT);
        item.addActionListener(listener);
        helpMenu.add(item);
        menuBar.add(helpMenu);

        this.setJMenuBar(menuBar);

        Verbose.finest("Exiting buildMenu(String[])");
    } // buildMenu()

    /**
     * Hides the Log View.
     */
    private void hideLogView() {
        Verbose.finer("Hide Log View");
        this.mLogViewMenuItem.setSelected(false);
        this.mDividerLocation = OverallView.this.mRightPane
                .getDividerLocation();
        this.mRightPane.remove(OverallView.this.mLogView);
    } // hideLogView()

    /**
     * Shows the Log View.
     */
    private void showLogView() {
        Verbose.finer("Show Log View");
        this.mLogViewMenuItem.setSelected(true);
        this.mRightPane.setRightComponent(OverallView.this.mLogView);
        this.mRightPane.setDividerLocation(this.mDividerLocation);
    } // showLogView()

    private static BufferedImage getImage(String strImgName) throws IOException {
        BufferedImage img;

        strImgName = strImgName + DEFAULT_IMG_EXTENSION;
        try {
            img = ImageIO.read(new File(strImgName));
        } catch (final Exception e) {
            try {
                final URL url = OverallView.class.getClassLoader().getResource(
                        strImgName);

                img = ImageIO.read(url);
            } catch (final IOException ioe) {
                throw ioe;
            }
        } // end try

        return img;
    } // getImage()

    /**
     * Display the DCs.
     * 
     * @author aas
     */
    class MyPanel extends JPanel {
        /**
         * A version number for this class so that serialisation can occur
         * without worrying about the underlying class changing between
         * serialisation and deserialisation.
         * <p>
         *
         * Not that we ever serialise this class of course, but JPanel
         * implements Serializable, so therefore by default we do as well.
         */
        private static final long serialVersionUID = 5424203044070513888L;

        private BufferedImage mBgImage;
        private int miBgX = -1;
        private int miDcX;
        private int miDcHalfWidth;

        public MyPanel(final int iWidth, final BufferedImage bgImage)
                throws IOException {
            setBackground(Color.WHITE);
            if (bgImage == null) {
                this.mBgImage = null;
            } else {
                this.mBgImage = bgImage;
            }
            setBorder(BorderFactory.createEtchedBorder());

            OverallView.this.getRootPane().addComponentListener(
                    new ComponentAdapter() {
                        public void componentResized(final ComponentEvent e) {
                            MyPanel.this.miBgX = -1;
                        } // componentResized()
                    });
        } // Constructor ()

        @Override
        public void paintComponent(final Graphics g) {
            super.paintComponent(g);

            if (this.miBgX == -1) {
                this.miBgX = (OverallView.this.getWidth() - MyPanel.this.mBgImage
                        .getWidth()) / 2;
                this.miDcX = (OverallView.this.getWidth() - OverallView.this.mImages
                        .get(0).getWidth()) / 2;
                this.miDcHalfWidth = OverallView.this.mImages.get(0).getWidth() / 2;
            }
            if (this.mBgImage != null) {
                g.drawImage(this.mBgImage, this.miBgX, -30, null);
            }

            final Font fontID = new Font(OverallView.this.mstrIdFontName,
                    OverallView.this.miIdFontStyle,
                    OverallView.this.miIdFontSize);
            final Font fontValue = new Font(OverallView.this.mstrValueFontName,
                    OverallView.this.miValueFontStyle,
                    OverallView.this.miValueFontSize);
            FontMetrics fontMetrics;
            int width;

            for (int i = 0; i < OverallView.this.mDCs.size(); ++i) {
                final DCData data = OverallView.this.mDCs.get(i);
                final int iX = this.miDcX + data.getX() + this.miDcHalfWidth;
                final int iY = data.getY() + 32;
                final Image image;
                int iLevel;

                g.setColor(Color.BLACK);

                // Draw connections
                for (int j = i + 1; j < OverallView.this.mDCs.size(); ++j) {
                    final DCData data1 = OverallView.this.mDCs.get(j);

                    g.drawLine(iX, iY,
                            this.miDcX + this.miDcHalfWidth + data1.getX(),
                            data1.getY() + 32);
                }

                // Draw DCs
                if (data.hasReplica()) {
                    iLevel = data.getLevel();
                    if (iLevel < 0) {
                        iLevel = -2;
                    }
                } else {
                    iLevel = -1;
                }
                image = OverallView.this.mImages.get(iLevel);
                g.drawImage(image, this.miDcX + data.getX(), data.getY(), this);

                // Draw value
                if (data.getDC().getData() != null) {
                    g.setFont(fontValue);
                    fontMetrics = g.getFontMetrics();
                    width = fontMetrics.stringWidth(data.getID());
                    g.drawString(data.getDC().getData().toString(), iX
                            - this.miDcHalfWidth + 6, data.getY()
                            + OverallView.this.miImgHeight + 10);
                }

                // Draw name
                g.setColor(Color.WHITE);
                g.setFont(fontID);
                fontMetrics = g.getFontMetrics();
                width = fontMetrics.stringWidth(data.getID());
                g.drawString(data.getID(), iX - width / 2, data.getY() + 15);
            } // end for
        } // paintComponent()
    } // end class MyPanel

    /**
     * Holds the data representing a visual DC.
     * 
     * @author aas
     */
    final class DCData {
        private final String mstrName;
        private final String mstrID;
        private final String mstrBageImgName;
        private final KeyStroke mReadKeyStroke;
        private final KeyStroke mWriteKeyStroke;
        private final int miX;
        private final int miY;
        private int miLevel;
        private DataCentre mDC;
        /** The time from where the read key will be accepted in nanoseconds. */
        private long mlReadDelayTime;
        /** The time from where the write key will be accepted in nanoseconds. */
        private long mlWriteDelayTime;

        DCData(final Element node) throws IOException {
            final Element keys;
            final String strReadKey;
            final String strWriteKey;

            this.mstrID = ((Element) node.getParentNode()).getAttribute("id");
            this.mstrName = node.getAttribute("name");
            this.mstrBageImgName = node.getAttribute("img");
            this.miX = Integer.parseInt(node.getAttribute("x"));
            this.miY = Integer.parseInt(node.getAttribute("y"));

            keys = XmlHelper.getNode(node.getChildNodes(), "keys");
            strReadKey = XmlHelper.getValue(keys.getChildNodes(), "name",
                    "read");
            strWriteKey = XmlHelper.getValue(keys.getChildNodes(), "name",
                    "write");

            this.mReadKeyStroke = KeyStroke.getKeyStroke(strReadKey);
            this.mWriteKeyStroke = KeyStroke.getKeyStroke(strWriteKey);
            this.miLevel = 0;
            this.mDC = (DataCentre) OverallView.this.mCtrl.get(getID());

            // Pre-load images
            if (OverallView.this.mImages.get(-1) == null) {
                final BufferedImage img = getImage(this.mstrBageImgName);

                OverallView.this.mImages.put(-1, img);
            }
            if (OverallView.this.mImages.get(-2) == null) {
                final BufferedImage img = getImage(this.mstrBageImgName + "-");

                OverallView.this.mImages.put(-2, img);
            }
            for (int i = 0; i < 10; ++i) {
                if (OverallView.this.mImages.get(i) == null) {
                    final BufferedImage img = getImage(this.mstrBageImgName + i);

                    OverallView.this.mImages.put(i, img);
                }
            } // end for
        } // Constructor ()

        public String getName() {
            return this.mstrName;
        } // getName()

        public String getID() {
            return this.mstrID;
        } // getID()

        public int getX() {
            return this.miX;
        } // getX()

        public int getY() {
            return this.miY;
        } // getY()

        public String getBaseImgName() {
            return this.mstrBageImgName;
        } // getBaseImgName()

        public KeyStroke getReadKeyStroke() {
            return this.mReadKeyStroke;
        } // getReadKeyStroke()

        public KeyStroke getWriteKeyStroke() {
            return this.mWriteKeyStroke;
        } // getWriteKeyStroke()

        public int getLevel() {
            return this.miLevel;
        } // getLevel()

        public boolean hasReplica() {
            return this.mDC.isReplicated();
        } // hasReplica()

        public void setLevel(final int iLevel) {
            this.miLevel = iLevel;
        } // setLevel()

        @Override
        public String toString() {
            return this.mstrID;
        } // toString()

        protected long getReadKeyDelay() {
            return this.mlReadDelayTime;
        } // getReadKeyDelay()

        protected void setReadKeyDelay(final long lTime) {
            this.mlReadDelayTime = lTime + KEY_STROKE_DELAY;
        } // setReadKeyDelay()

        protected long getWriteKeyDelay() {
            return this.mlWriteDelayTime;
        } // getWriteKeyDelay()

        protected void setWriteKeyDelay(final long lTime) {
            this.mlWriteDelayTime = lTime + KEY_STROKE_DELAY;
        } // setWriteKeyDelay()

        DataCentre getDC() {
            return this.mDC;
        } // getDC()
    } // end class DCData

    /**
     * Listener.
     * 
     * @author aas
     */
    class MyListener implements ActionListener, KeyEventDispatcher {
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final String strAction = ae.getActionCommand();

            if (strAction.equals(LogGUIView.ACTION_CLEAR)) {
                return;
            }

            if (strAction.equals(LogGUIView.ACTION_HELP)) {
                help();
            } else if (strAction.equals(ACTION_MENU_HELP_ABOUT)) {
                final AboutDlg about = new AboutDlg(OverallView.this,
                        ABOUT_NAME, ABOUT_IMG, ABOUT_TOOLTIP, ABOUT_COPYRIGHT,
                        ABOUT_INFO);

                about.setVisible(true);
            } else if (strAction.equals(ACTION_MENU_HELP_CONTENT)) {
                // Title, Frame, Contents Names, Contents Filenames
                final HelpDlg help = new HelpDlg(CONTENT_TITLE,
                        OverallView.this, CONTENT_NAMES, CONTENT_FILENAMES,
                        CONTENT_IMG);

                help.setVisible(true);
            } else if (strAction.equals(LogGUIView.ACTION_CLOSE)) {
                OverallView.this.hideLogView();
            }
        } // actionPerformed()

        @Override
        public boolean dispatchKeyEvent(final KeyEvent ke) {
            if (OverallView.this.mTests != null) {
                return false;
            }

            final KeyStroke keyStroke = KeyStroke.getKeyStroke(ke.getKeyCode(),
                    ke.getModifiers());

            for (final DCData data : OverallView.this.mDCs) {
                if (keyStroke == data.getReadKeyStroke()) {
                    final long time = System.currentTimeMillis();

                    if (time > data.getReadKeyDelay()) {
                        // Read
                        final int iID = OverallView.this.mMsgProcessor
                                .getNextMsgID();

                        Verbose.info(
                                "Request {3} from {0} to {1} with ID {2,number,####}",
                                getID(), data.getID(), iID, TYPE.READ);
                        OverallView.this.mCtrl
                                .send(data.getID(), new ReadRequest<>(
                                        OverallView.this.getID(), iID));
                        data.setReadKeyDelay(time);
                    }

                    return true;
                } else if (keyStroke == data.getWriteKeyStroke()) {
                    final long time = System.currentTimeMillis();

                    if (time > data.getWriteKeyDelay()) {
                        // Write
                        final int iID = OverallView.this.mMsgProcessor
                                .getNextMsgID();

                        Verbose.info(
                                "Request {3} from {0} to {1} with ID {2,number,####}",
                                getID(), data.getID(), iID, TYPE.WRITE);
                        OverallView.this.mCtrl.send(data.getID(),
                                new WriteRequest<>(OverallView.this.getID(),
                                        iID, keyStroke.toString()));
                        data.setWriteKeyDelay(time);
                    }

                    return true;
                }
            } // end for

            return false;
        } // dispatchKeyEvent()
    } // end class MyListener
} // end class OverallView
