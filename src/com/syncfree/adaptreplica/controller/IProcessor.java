package com.syncfree.adaptreplica.controller;

/**
 * Interface for the processor responsible to process the messages sent to it.
 * 
 * @author aas
 */
public interface IProcessor {
    /**
     * Processes the provided message.
     * 
     * @param msg
     *            the message.
     * @return true to exit or false otherwise.
     */
    public boolean process(final Message<?> msg);

    /**
     * @return the processor ID.
     */
    public String getID();

    /**
     * Called just before the message processing thread is started to allow some
     * initialisations before starts.
     */
    public void onStarting();

    /**
     * Called just before the message processing thread is stopped to allow some
     * cleaning up before stops.
     */
    public void onStopping();
} // end interface IProcessor
