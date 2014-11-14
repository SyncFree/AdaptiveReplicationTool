package com.syncfree.adaptreplica.controller;

import com.syncfree.adaptreplica.test.ITestCtrler;

/**
 * .
 * 
 * @author aas
 */
public interface IController extends IResetable {
    /**
     * Searches for the specified element.
     * 
     * @param strID the element ID.
     * @return the element if exists, or null otherwise.
     */
    public IMessageListener get(String strID);

    /**
     * Sends the specified message to the specified destination.
     * 
     * @param destinamtionID the destination ID.
     * @param msg the message.
     */
    public void send(String destinamtionID, Message<?> msg);

    /**
     * Sends the specified message to all the DCs.
     * 
     * @param msg the message.
     * @param astrExceptIDs this of DC not to send the message to.
     */
    public void sendAll(final Message<?> msg, final String... astrExceptIDs);

    /**
     * Sets the running controller.
     * 
     * @param testCtrler the running controller.
     */
    public void setTestsCtrler(final ITestCtrler testCtrler);
} // end interface IController
