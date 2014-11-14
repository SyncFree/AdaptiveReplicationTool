package com.syncfree.adaptreplica.algorithm;

import org.w3c.dom.Element;

import com.syncfree.adaptreplica.controller.DataCentre;
import com.syncfree.adaptreplica.test.ITestCtrler;

/**
 * Support class to extend algorithm and give some assurances that the
 * appropriate constructor is implements.
 * 
 * @author aas
 */
public abstract class Algorithm implements IAlgorithm {
    /** The data centre. */
    private DataCentre mDC;
    /** Controls execution, i.e. time delays. */
    protected ITestCtrler mTestCtrler;
    /** The current level. */
    private int miCurrentLevel;

    /**
     * The listener to notify of changes in the level of the replication
     * strength.
     */
    private IAlgorithmListener mListener;

    protected Algorithm(final Element arguments) {
        this.miCurrentLevel = Integer.MIN_VALUE;
    } // Constructor ()

    protected <T extends Algorithm> Algorithm(final DataCentre dc) {
        this.mDC = dc;
    } // Constructor ()

    @Override
    public void setTestsCtrler(final ITestCtrler testCtrler) {
        this.mTestCtrler = testCtrler;
    } // setTestsCtrler()

    @Override
    public void setListener(final IAlgorithmListener listener) {
        this.mListener = listener;
    } // setListener()

//    /**
//     * @return true if replicas may be reduced or false otherwise.
//     */
//    protected final boolean couldReduceReplicas() {
//        return this.mDC.couldReduceReplicas();
//    } // couldReduceReplicas()

    /**
     * @return the data centre which uses this algorithm.
     */
    protected final DataCentre getDC() {
        return this.mDC;
    } // getDC()

    /**
     * @return the ID of the data centre.
     */
    protected String getDCID() {
        return this.mDC.getID();
    } // getDCID()

    /**
     * @return true if the data centre contains a replica or false otherwise.
     */
    protected final boolean hasDCReplica() {
        return this.mDC.isReplicated();
    } // hasDCReplica()

    /**
     * @return true if there is a level listener registered or false otherwise.
     */
    protected final boolean hasLevelListener() {
        return (this.mListener != null);
    } // hasLevelListener()

    /**
     * Sends a request.
     * 
     * @param value the value to send in the request.
     */
    protected <T> void sendAlgorithmRequest(final T value) {
        this.mDC.sendAlgorithmRequest(value);
    } // sendAlgorithmRequest()

    /**
     * Sends the level of replication to the registered listener.
     * 
     * @param iLevel the level of replication.
     * @param bOverride true to notify irrespective of other values.
     */
    protected final void sendLevel(final int iLevel, final boolean bOverride) {
        if (bOverride || (this.mListener != null && iLevel != this.miCurrentLevel)) {
            this.mListener.onChange(this.mDC, iLevel);
            this.miCurrentLevel = iLevel;
        }
    } // sendLevel()
} // end class Algorithm
