package com.syncfree.adaptreplica.controller;

/**
 * Representation of a request sent by an algorithm.
 * 
 * @author aas
 */
public class AlgorithmRequest<T> extends Message<T> {
    public AlgorithmRequest(final String strSenderID, final int iId, final T value) {
        super(strSenderID, iId, TYPE.ALGORITHM, value);
    } // Constructor ()
} // end class AlgorithmRequest
