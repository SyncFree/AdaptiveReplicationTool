package com.syncfree.adaptreplica.controller;

/**
 * Representation of a remove replica request.
 * 
 * @author aas
 */
public class RemoveReplicaRequest extends Message<Object> {
    public RemoveReplicaRequest(final String strSenderID, final int iID) {
        super(strSenderID, iID, TYPE.REMOVED_REPLICA, null);
    } // Constructor ()
} // end class RemoveReplicaRequest
