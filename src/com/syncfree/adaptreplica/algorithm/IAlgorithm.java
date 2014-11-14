package com.syncfree.adaptreplica.algorithm;

import org.w3c.dom.Element;

import com.syncfree.adaptreplica.controller.DataCentre;
import com.syncfree.adaptreplica.controller.IDataHolder;
import com.syncfree.adaptreplica.controller.IResetable;
import com.syncfree.adaptreplica.controller.Message;

/**
 * Basic interface for an algorithm.
 * 
 * Any algorithm must extend the Algorithm class.
 * 
 * @author aas
 */
public interface IAlgorithm extends IResetable, ILevelCtrl {
    /**
     * Applies the instructions in the specified message.
     * 
     * @param msg the message.
     * @return true if the copy still exists here of false otherwise.
     */
    public boolean apply(Message<?> msg);

    /**
     * Called when a replica of the data is placed in the DC.
     */
    public void dataWasSet();

    /**
     * Processes a read request.
     */
    public void otherRead(IDataHolder dc);
    /**
     * Processes a read request by another data centre.
     */
    public void ownRead();

    /**
     * Processes a write request.
     * 
     * @param data the data.
     * @return true if the copy still exists here of false otherwise.
     */
    public boolean ownWrite(Object data);
    /**
     * Processes a write request by another data centre.
     * 
     * @param dc .
     * @param data the data.
     * @return true if the copy still exists here or false otherwise.
     */
    public boolean otherWrite(IDataHolder dc, Object data);

    /**
     * @param dc the data centre.
     * @param algorithm the specific argument for the new instance of the algorithm.
     * @return a duplicate of this algorithm.
     */
    public abstract IAlgorithm clone(Element algorithm, DataCentre dc);

    /**
     * @return true if the data should be replicated or false otherwise.
     */
    public boolean isReplicated();

    /**
     * Sets the listener for changes in the replica strength.
     * 
     * @param listener the listener.
     */
    public void setListener(IAlgorithmListener listener);

    /**
     * Requests to start processing.
     */
    public void start();
 
    /**
     * Requests to stop any processing.
     */
    public void stop();
} // end class IAlgorithm
