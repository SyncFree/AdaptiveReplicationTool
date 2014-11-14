package com.syncfree.adaptreplica.controller;

/**
 * .
 *
 * @author aas
 */
public class ForwardRequest<T> extends Request<T> {
    private final Request<T> mRequest;

    public ForwardRequest(final String strID, final int iID, final Request<T> request) {
        super(strID, iID, request.getType(), request.getValue());
        this.mRequest = request;
    } // Constructor ()

    public Request<T> getBaseRequest() {
        return this.mRequest;
    } // getValue()

    public String getRedirectionID() {
        return this.mRequest.getUID();
    } // getRedirectionID()
} // end ForwardRequest
