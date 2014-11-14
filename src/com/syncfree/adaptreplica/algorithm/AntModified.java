package com.syncfree.adaptreplica.algorithm;

import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.controller.DataCentre;
import com.syncfree.adaptreplica.controller.IDataHolder;
import com.syncfree.adaptreplica.controller.IResetable;
import com.syncfree.adaptreplica.controller.Message;
import com.syncfree.adaptreplica.test.ITestCtrler;
import com.syncfree.adaptreplica.util.XmlHelper;

/**
 * Implementation of the ant adaptation algorithm.
 * 
 * @author aas
 */
public class AntModified extends Algorithm {
    private static final int DEFAULT_TIME_DECAY_SLEEP = 1000;

    // Algorithm arguments
    private final double mdReadStrengthIncr;
    private final double mdWriteStrengthIncr;
    private final double mdWriteOtherDcStrengthDecay;
    private final double mdReplicationThreshold;
    private final Double mdTimeDecay;
    private final int miTimeDecaySleep;
    /** The strength of the replica. */
    private final double mdStrengthLimit;

    private final double mdLevelWidth;

    // Variables
    /** The strength of the replication signal. */
    private double mdStrength;
    /** Specify if the DC should have a replica. */
    private boolean mbReplicated;
    /** The time decay processor. */
    private TimeDecayProcessor mTimeDecayProcessor;
    
    // Original values
    /** The original strength of the replication signal. */
    private double mdOrigStrength;
    /** Specify if the DC should have a replica, original value. */
    private boolean mbOrigReplicated;

    /**
     * Builds the algorithm using the arguments provided.
     * 
     * @param arguments
     *            the arguments for the algorithm.
     */
    public AntModified(final Element arguments) {
        this(XmlHelper.getNode(arguments.getChildNodes(), "args")
                .getChildNodes());
    } // Constructor ()

    /**
     * Builds the algorithm using the arguments provided.
     * 
     * @param arguments
     *            the arguments for the algorithm.
     */
    public AntModified(final NodeList arguments) {
        this(XmlHelper.getArgDouble(arguments, "readStrengthIncr"),
                XmlHelper.getArgDouble(arguments, "writeStrengthIncr"),
                XmlHelper.getArgDouble(arguments, "writeOtherDcStrengthDecay"),
                XmlHelper.getArg(arguments, "replicationThreshold", 0.0),
                XmlHelper.getArg(arguments, "timeDecay", 0.0), XmlHelper
                        .getArgAttr(arguments, "timeDecay", "time_msec",
                                DEFAULT_TIME_DECAY_SLEEP), XmlHelper.getArg(
                        arguments, "strengthLimit", Double.POSITIVE_INFINITY),
                XmlHelper.getArg(arguments, "strength", 0.0), null);
    } // Constructor ()

    /**
     * Builds the algorithm using the arguments provided.
     * 
     * @param dReadStrengthIncr
     *            .
     * @param dWriteStrengthIncr
     *            .
     * @param dWriteOtherDcStrengthDecay
     *            .
     * @param dReplicationThreshold
     *            .
     * @param dTimeDecay
     *            .
     * @param dStrengthLimit
     *            .
     */
    public AntModified(final double dReadStrengthIncr, final double dWriteStrengthIncr,
            final double dWriteOtherDcStrengthDecay,
            final double dReplicationThreshold, final double dTimeDecay,
            final int iTimeDecaySleep, final double dStrengthLimit,
            final double dStrength, final DataCentre dc) {
        super(dc);

        this.mdReadStrengthIncr = dReadStrengthIncr;
        this.mdWriteStrengthIncr = dWriteStrengthIncr;
        this.mdWriteOtherDcStrengthDecay = dWriteOtherDcStrengthDecay;
        this.mdReplicationThreshold = dReplicationThreshold;
        this.mdTimeDecay = dTimeDecay;
        this.miTimeDecaySleep = iTimeDecaySleep;
        this.mdStrengthLimit = dStrengthLimit;
        this.mbReplicated = this.mbOrigReplicated = false;
        this.mdStrength = this.mdOrigStrength = dStrength;
        this.mdLevelWidth = (this.mdStrengthLimit - this.mdReplicationThreshold) / 9;
    } // Constructor ()

    /**
     * Builds the algorithm as copy of the passed base one for the given DC.
     * 
     * @param alg
     *            the base algorithm.
     * @param dc
     *            the data centre.
     */
    protected AntModified(final AntModified alg, final Element arguments,
            final DataCentre dc) {
        this(alg.mdReadStrengthIncr, alg.mdWriteStrengthIncr,
                alg.mdWriteOtherDcStrengthDecay, alg.mdReplicationThreshold,
                alg.mdTimeDecay, alg.miTimeDecaySleep, alg.mdStrengthLimit, alg
                        .getStrength(), dc);

        if (arguments != null) {
            if (hasDCReplica()) {
                final Element args = XmlHelper.getNode(arguments, "args");

                if (args != null) {
                    this.mdStrength = this.mdOrigStrength = XmlHelper.getValue(args, "name",
                            "strength", this.mdStrength);
                    this.mbReplicated = this.mbOrigReplicated = (this.mdStrength > 0);
                }
            }
        }
    } // Constructor ()

    /**
     * @return the replication current strength.
     */
    public double getStrength() {
        return this.mdStrength;
    } // getStrength()

    @Override
    public boolean isReplicated() {
        return this.mbReplicated;
    } // isReplicated()

    @Override
    public void otherRead(final IDataHolder dc) {
        // Nothing to do
    } // otherRead()

    @Override
    public void ownRead() {
        updateInc(this.mdReadStrengthIncr);
    } // ownRead()

    @Override
    public boolean ownWrite(final Object data) {
        updateInc(this.mdWriteStrengthIncr);

        return this.mbReplicated;
    } // ownWrite()

    @Override
    public boolean otherWrite(final IDataHolder dc, final Object data) {
        return updatDecay(this.mdWriteOtherDcStrengthDecay, true);
    } // otherWrite()

    @Override
    public AntModified clone(final Element algorithm, final DataCentre dc) {
        return new AntModified(this, algorithm, dc);
    } // clone()

    @Override
    public synchronized void start() {
        this.mTimeDecayProcessor = new TimeDecayProcessor(this.mTestCtrler);
        if (hasLevelListener() && this.mdStrength > 0) {
            setLevel(true);
        }
        this.mTimeDecayProcessor.start();
    } // start()

    @Override
    public synchronized void stop() {
        this.mTimeDecayProcessor.stopIt();
        this.mTimeDecayProcessor = null;
    } // stop();

    @Override
    public boolean apply(final Message<?> msg) {
        return updatDecay((Double) msg.getValue(), false);
    } // apply()

    @Override
    public void dataWasSet() {
        setLevel(true);
    } // dataWasSet()

    @Override
    public boolean reset(final IResetable initiator) {
        this.mdStrength = this.mdOrigStrength;
        this.mbReplicated = this.mbOrigReplicated;

        return false;
    } // reset()

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + getDCID() + "]";
    } // toString()

    /**
     * Applies the specified increment in strength and notify listener, if any,
     * of the change in the replication strength.
     * 
     * @param dIncrStreangth
     *            the increment strength.
     */
    protected void updateInc(final double dIncrStreangth) {
        if (this.mdStrength < this.mdStrengthLimit) {
            this.mdStrength += dIncrStreangth;
            if (this.mbReplicated == false) {
                this.mbReplicated = (this.mdStrength > this.mdReplicationThreshold);
            } else {
                this.mdStrength = Math.min(this.mdStrength,
                        this.mdStrengthLimit);
            }

            if (hasLevelListener()) {
                setLevel(false);
            }
        }
    } // updateInc()
    
    @Override
    public int getLevel() {
        final int iLevel;

        if (this.mbReplicated) {
            if (this.mdStrength <= this.mdReplicationThreshold) {
                iLevel = 0;
            } else {
                iLevel = 1 + (int) ((this.mdStrength
                        - this.mdReplicationThreshold - 0.0001) / this.mdLevelWidth);
            }
        } else {
            // No replica
            iLevel = -1;
        }

        return iLevel;
    } // getLevel()
    
    @Override
    public void updateLevel() {
        setLevel(true);
    } // setLevel()

    protected void setLevel(final boolean bOverride) {
        sendLevel(getLevel(), bOverride);
    } // setLevel()

    /**
     * Applies the specified decrement in strength and notify listener, if any,
     * of the change in the replication strength.
     * 
     * @param dStrengthDecay
     *            the strength decay.
     */
    protected boolean updatDecay(final double dStrengthDecay, final boolean bOverride) {
        if (this.mdStrength > 0) {
            this.mdStrength -= dStrengthDecay;
            if (this.mdStrength < 0) {
                this.mdStrength = 0;
            }
        }
        if (this.mbReplicated) {
            this.mbReplicated = (this.mdStrength > 0);
            if (hasLevelListener()) {
                setLevel(bOverride);
            }
        }

        return this.mbReplicated;
    } // updatDecay()

    /**
     * The processor for the time decay.
     * 
     * This thread should not modify directly the strength or any other variable
     * from the AntAlgorithm.
     * 
     * @author aas
     */
    class TimeDecayProcessor extends Thread {
        private final ITestCtrler mTestCtrler;
        private boolean mbRunning;

        TimeDecayProcessor(final ITestCtrler testCtrler) {
            super("TimeDecay-" + getDCID());

            this.mTestCtrler = testCtrler;
        } // Constructor ()

        @Override
        public void run() {
            synchronized (this) {
                if (this.mbRunning) {
                    Verbose.warning("Already running {0}", getName());

                    return;
                }
                this.mbRunning = true;
            }

            try {
                while (this.mbRunning) {
                    Thread.sleep(AntModified.this.miTimeDecaySleep);
                    if (AntModified.this.mdStrength > 0
                            && this.mTestCtrler.next(true)) {
                        sendAlgorithmRequest(AntModified.this.mdTimeDecay);
                    }
                } // end while
            } catch (InterruptedException ie) {
                Verbose.log(Level.WARNING, ie,
                        "Force to stop time decay process \"{0}\"; {1}",
                        getName(), ie.getMessage());
            } // end try
            this.mbRunning = false;
        } // run()

        /**
         * Requests to stop the time decay.
         */
        public synchronized void stopIt() {
            this.mbRunning = false;
        } // stopIt()
    } // end class TimeDecayProcessor
} // end class AntModified
