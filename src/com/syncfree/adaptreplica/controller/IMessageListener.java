package com.syncfree.adaptreplica.controller;

import com.syncfree.adaptreplica.test.ITestCtrler;


/**
 * Communication between threads.
 *
 * @author aas
 */
public interface IMessageListener {
    /**
     * @return the ID.
     */
    public String getID();

    /**
     * Sends the specified message to the current thread.
     * 
     * @param message
     *            the message.
     */
    public void send(Message<?> message);

    /**
     * Starts the listener.
     */
    public void start();

    /**
     * Stops the listener.
     */
    public void stop();

    /**
     * Sets the running controller.
     * 
     * @param testCtrler the running controller.
     */
    public void setTestsCtrler(final ITestCtrler testCtrler);
} // end interface IMessageListener
