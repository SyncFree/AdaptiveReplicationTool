package com.syncfree.adaptreplica.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.aasco.util.ImageManager;
import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.test.ITestCtrler;
import com.syncfree.adaptreplica.test.ITestsRunner;

/**
 * Display of the player view.
 * 
 * @author aas
 */
public class PlayerView extends JPanel implements ActionListener, ITestCtrler {
    /**
     * A version number for this class so that serialisation can occur without
     * worrying about the underlying class changing between serialisation and
     * deserialisation.
     * <p>
     *
     * Not that we ever serialise this class of course, but JFrame implements
     * Serializable, so therefore by default we do as well.
     */
    private static final long serialVersionUID = -4782620029786819449L;

    // Actions
    private static final String TOOLTIP_PLAY = ACTION.PLAY.getTooltip();
    private static final String TOOLTIP_NEXT = ACTION.NEXT.getTooltip();
    private static final String TOOLTIP_PAUSE = ACTION.PAUSE.getTooltip();
    private static final String TOOLTIP_STOP = ACTION.STOP.getTooltip();

    // Actions
    private static final String ACTION_PLAY = ACTION.PLAY.name();
    private static final String ACTION_NEXT = ACTION.NEXT.name();
    private static final String ACTION_PAUSE = ACTION.PAUSE.name();
    private static final String ACTION_STOP = ACTION.STOP.name();

    // Buttons
    private final JButton mBPlay;
    private final JButton mBNext;
    private final JButton mBPause;
    private final JButton mBStop;

    private ACTION mState;
    private ITestsRunner mTestRunner;

    public PlayerView(String strStartState) {
        this.mBPlay = new JButton(ImageManager.getImage("images/play.png"));
        this.mBPlay.setBorder(BorderFactory.createEmptyBorder());
        this.mBPlay.setToolTipText(TOOLTIP_PLAY);
        this.mBPlay.setActionCommand(ACTION_PLAY);
        this.mBPlay.addActionListener(this);
        add(this.mBPlay);

        this.mBPause = new JButton(ImageManager.getImage("images/pause.png"));
        this.mBPause.setBorder(BorderFactory.createEmptyBorder());
        this.mBPause.setToolTipText(TOOLTIP_PAUSE);
        this.mBPause.setActionCommand(ACTION_PAUSE);
        this.mBPause.addActionListener(this);
        add(this.mBPause);

        this.mBNext = new JButton(ImageManager.getImage("images/next.png"));
        this.mBNext.setBorder(BorderFactory.createEmptyBorder());
        this.mBNext.setToolTipText(TOOLTIP_NEXT);
        this.mBNext.setActionCommand(ACTION_NEXT);
        this.mBNext.addActionListener(this);
        add(this.mBNext);

        this.mBStop = new JButton(ImageManager.getImage("images/stop.png"));
        this.mBStop.setBorder(BorderFactory.createEmptyBorder());
        this.mBStop.setToolTipText(TOOLTIP_STOP);
        this.mBStop.setActionCommand(ACTION_STOP);
        this.mBStop.addActionListener(this);
        add(this.mBStop);

        setBorder(BorderFactory.createEtchedBorder());

        if (strStartState == null) {
            this.mState = ACTION.STOP;
        } else {
            strStartState = strStartState.toUpperCase();
            if (strStartState.equals(ACTION_PLAY)) {
                this.mState = ACTION.PLAY;
            } else if (strStartState.equals(ACTION_PAUSE)) {
                this.mState = ACTION.PAUSE;
            } else if (strStartState.equals(ACTION_NEXT)) {
                this.mState = ACTION.NEXT;
            } else if (strStartState.equals(ACTION_STOP)) {
                this.mState = ACTION.STOP;
            } else {
                this.mState = ACTION.STOP;
                Verbose.warning(
                        "Starting state \"{0}\" is not soported; using {1} instead",
                        strStartState, this.mState);
            }
        }
        setEnable();
    } // Constructor ()

    @Override
    public ACTION getCurrentState() {
        return null;
    } // getCurrentState()

    public void initilise(final ITestsRunner testRunner) {
        this.mTestRunner = testRunner;
    } // initialise()

    public boolean next(final boolean bAnymore) {
        if (!bAnymore) {
            waiting();
        }

        if (this.mState == ACTION.PLAY) {
            return true;
        } else if (this.mState == ACTION.NEXT) {
            this.mState = ACTION.PAUSE;

            return true;
        } else if (this.mState == ACTION.PAUSE) {
            boolean bContinue = waiting();

            if (this.mState == ACTION.NEXT) {
                this.mState = ACTION.PAUSE;
            }

            return bContinue;
        } else if (this.mState == ACTION.STOP) {
            boolean bContinue = waiting();

            return bContinue;
        } else {
            Verbose.warning("Invalid state {0}; stopping", this.mState);
            throw new RuntimeException("Invalid state " + this.mState
                    + "; exiting");
        }
    } // next()

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final String strAction = ae.getActionCommand();

        if (strAction.equals(ACTION_PLAY)) {
            if (this.mState == ACTION.STOP) {
                this.mTestRunner.reset(null);
            }
            this.mState = ACTION.PLAY;
            setEnable();
            synchronized (this) {
                notifyAll();
            } // end synchronized
        } else if (strAction.equals(ACTION_NEXT)) {
            if (this.mState == ACTION.STOP) {
                this.mTestRunner.reset(null);
            }
            this.mState = ACTION.NEXT;
            setEnable();
            synchronized (this) {
                notify();
            } // end synchronized
        } else if (strAction.equals(ACTION_PAUSE)) {
            this.mState = ACTION.PAUSE;
            setEnable();
        } else if (strAction.equals(ACTION_STOP)) {
            if (this.mState == ACTION.STOP) {
                Verbose.warning("ALready set {0}", strAction);
            }
            this.mState = ACTION.STOP;
            setEnable();
        } else {
            Verbose.warning("Unknown action {0}", strAction);
        }
    } // actionPerformed()

    public void executeAction(final ACTION action) {
        if (action != null) {
            if (this.mState == action) {
                // Already set
                Verbose.finer("Ignoring action \"{0}\"", action);

                return;
            }

            final ActionEvent evt = new ActionEvent(getButton(action), 1,
                    action.name());

            // Maybe it should be executed by the GUI node
            actionPerformed(evt);
        }
    } // executeAction()

    /**
     * @param action the action.
     * @return the button associated with the specified action.
     */
    protected JButton getButton(final ACTION action) {
        switch (action) {
        case PLAY:
            return this.mBPlay;

        case PAUSE:
            return this.mBPause;

        case NEXT:
            return this.mBNext;

        default:
            return this.mBStop;
        } // end switch
    } // getButton()

    /**
     * @return true to continue and false otherwise;
     */
    protected boolean waiting() {
        try {
            synchronized (this) {
                wait();
            } // end synchronized

            return (this.mState != null);
        } catch (InterruptedException ie) {
            Verbose.log(Level.SEVERE, ie, "Error when waiting on {0}; {1}",
                    this.mState, ie.getMessage());

            return false;
        } // end try
    } // waiting()

    private void setEnable() {
        if (this.mState == ACTION.PLAY) {
            this.mBPlay.setEnabled(false);
            this.mBPause.setEnabled(true);
            this.mBNext.setEnabled(false);
            this.mBStop.setEnabled(true);
        } else if (this.mState == ACTION.NEXT) {
            this.mBPlay.setEnabled(true);
            this.mBPause.setEnabled(false);
            this.mBNext.setEnabled(true);
            this.mBStop.setEnabled(true);
        } else if (this.mState == ACTION.PAUSE) {
            this.mBPlay.setEnabled(true);
            this.mBPause.setEnabled(false);
            this.mBNext.setEnabled(true);
            this.mBStop.setEnabled(true);
        } else if (this.mState == ACTION.STOP) {
            this.mBPlay.setEnabled(true);
            this.mBPause.setEnabled(false);
            this.mBNext.setEnabled(true);
            this.mBStop.setEnabled(false);
        }
    } // setEnable()
} // end class PlayerView
