package com.syncfree.adaptreplica.controller;

/**
 * .
 * 
 * @author aas
 */
public class HasReplicaRequest extends Message<Object> {
    public HasReplicaRequest(final String strSenderID, final int iId) {
        super(strSenderID, iId, TYPE.HAS_REPLICA, null);
    } // Constructor ()
} // end class HasReplicaRequest
