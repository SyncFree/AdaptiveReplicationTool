package com.syncfree.adaptreplica.controller;

/**
 * .
 * 
 * @author aas
 *
 * @param <T>
 */
public class ReadRequest<T> extends Request<T> {
    /**
     * Builds a read request.
     * 
     * @param strSenderID
     *            sender's ID.
     * @param iId
     *            the data centre ID.
     * @param value
     *            the value.
     */
    public ReadRequest(final String strSenderID,
            final int iId) {
        super(strSenderID, iId, TYPE.READ, null);
    } // Constructor ()

    public ReadRequest(final String strName, final String strSenderID,
            final ReadRequest<T> request) {
        super(strSenderID, request.getID(), TYPE.READ, request
                .getValue());
    } // Constructor ()
} // end class ReadRequest
