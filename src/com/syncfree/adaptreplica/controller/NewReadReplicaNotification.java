package com.syncfree.adaptreplica.controller;

import com.syncfree.adaptreplica.controller.Message;

/**
 * Representation of a new replica from a read.
 * 
 * @author aas
 */
public class NewReadReplicaNotification extends Message<Object> {
    public NewReadReplicaNotification(final String strSenderID, final int iId) {
        super(strSenderID, iId, TYPE.READ_NEW_REPLICA, null);
    } // Constructor ()
} // end class NewReadReplicaNotification
