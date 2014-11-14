package com.syncfree.adaptreplica.controller;

/**
 * Representation of a request from a thread.
 * 
 * @author aas
 */
public class Request<T> extends Message<T> {
    /**
     * Builds a request from the specified thread.
     * 
     * @param strSenderID
     *            the sender's ID.
     * @param iId
     *            the data centre ID.
     * @param type
     *            the message type.
     * @param value
     *            the value.
     */
    public Request(final String strSenderID,
            final int iId, final TYPE type, final T value) {
        super(strSenderID, iId, type, value);
    } // Constructor ()
} // end class Request
