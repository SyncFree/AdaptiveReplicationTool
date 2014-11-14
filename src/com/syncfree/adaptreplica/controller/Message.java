package com.syncfree.adaptreplica.controller;

/**
 * A general message.
 *
 * @author aas
 *
 * @param <T>
 */
public class Message<T> {
    protected TYPE mType;
    private final T mValue;
    private final int miId;
    private final String mstrSenderID;

    public enum TYPE {
        READ, READ_NEW_REPLICA, WRITE, WRITE_NEW_REPLICA, HAS_REPLICA, REMOVED_REPLICA, REQUEST, RESPONSE, STOP, ALGORITHM;
    } // end enum TYPE

    /**
     * Builds the message from its components.
     *
     * @param strSenderID the ID of the sender.
     * @param iId the message ID.
     * @param type the message type.
     * @param value the message value.
     */
    public Message(final String strSenderID, final int iId, final TYPE type, final T value) {
        this.miId = iId;
        this.mType  = type;
        this.mValue = value;
        this.mstrSenderID = strSenderID;
    } // Constructor ()

    /**
     * @return the message ID.
     */
    public int getID() {
        return this.miId;
    } // getID()

    /**
     * @return the sender ID plus the message ID.
     */
    public String getUID() {
        return this.mstrSenderID + this.miId;
    } // getUID()

    /**
     * @return the sender ID.
     */
    public String getSenderID() {
        return this.mstrSenderID;
    } // getSenderID()

    /**
     * @return the type of message.
     */
    public TYPE getType() {
        return this.mType;
    } // getType()

    /**
     * @return the message value.
     */
    public T getValue() {
        return this.mValue;
    } // getValue()

    @Override
    public String toString() {
        return this.mstrSenderID + "[" + getType() + ", "+ this.miId + "]";
    } // toString()
} // end class Message
