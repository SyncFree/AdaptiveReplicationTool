package com.syncfree.adaptreplica.controller;

import com.syncfree.adaptreplica.controller.Message.TYPE;

/**
 * Extension of the controller interface.
 * 
 * @author aas
 */
public interface IFullController extends IController {
    /**
     * 
     * @param listener
     */
    public void register(final IMessageListener listener);

    /**
     * Builds reply for the specified data centre with the specified components.
     * 
     * @param strCurrentID
     *            the data centre ID that sends the reply.
     * @param strDestinationID
     *            the destination data centre ID.
     * @param iId
     *            the message ID.
     * @param type
     *            the type of reply.
     * @param message
     *            the message.
     */
    public void reply(final String strCurrentID, final String strDestinationID,
            int iId, TYPE type, Object message);

    /**
     * 
     * @param listener
     */
    public void unregister(final IMessageListener listener);
} // end interface IFullController
