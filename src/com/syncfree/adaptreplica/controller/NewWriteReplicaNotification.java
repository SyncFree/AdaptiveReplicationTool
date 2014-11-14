package com.syncfree.adaptreplica.controller;

/**
 * Representation of a new replica from a write.
 * 
 * @author aas
 */
public class NewWriteReplicaNotification<T> extends Message<T> {
    public NewWriteReplicaNotification(final String strSenderID, final int iId,
            final T value) {
        super(strSenderID, iId, TYPE.WRITE_NEW_REPLICA, value);
    } // Constructor ()
} // end class NewWriteReplicaNotification