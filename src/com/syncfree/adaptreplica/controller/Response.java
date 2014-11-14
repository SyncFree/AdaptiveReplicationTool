package com.syncfree.adaptreplica.controller;

/**
 * Representation of a response from a thread.
 * 
 * @author aas
 *
 * @param <T>
 */
public class Response<T> extends Message<T> {
    private final TYPE mRequestType;
    private final String mstrRedirectionID;

    /**
     * Builds a reply from the specified thread.
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
    public Response(final String strSenderID, final int iId, final TYPE type,
            final T value) {
        this(strSenderID, iId, type, value, null);
    } // Constructor ()

    public Response(final String strSenderID, final int iId, final TYPE type,
            final T value, final String strRedirectionID) {
        super(strSenderID, iId, TYPE.RESPONSE, value);

        this.mRequestType = type;
        this.mstrRedirectionID = strRedirectionID;
    } // Constructor ()

    public TYPE getRequestType() {
        return this.mRequestType;
    } // getRequestType()

    public String getRedirectionID() {
        return this.mstrRedirectionID;
    } // getRedirectionID()
} // end class Reply
