package com.syncfree.adaptreplica.controller;

/**
 * .
 * 
 * @author aas
 */
public class WriteRequest<T> extends Request<T> {
    /**
     * Builds a write request.
     * 
     * @param strSenderID
     *            sender's ID.
     * @param iId
     *            the data centre ID.
     * @param value
     *            the value.
     */
    public WriteRequest(final String strSenderID, final int iId, final T value) {
        super(strSenderID, iId, TYPE.WRITE, value);
    } // Constructor ()

    public WriteRequest(final String strSenderID, final WriteRequest<T> request) {
        super(strSenderID, request.getID(), TYPE.WRITE, request.getValue());
    } // Constructor ()

    public void setType(final TYPE type) {
        this.mType = type;
    } // setType()
} // end class WriteRequest
