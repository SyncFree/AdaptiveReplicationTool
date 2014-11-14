package com.syncfree.adaptreplica.controller;


/**
 * Interface for classes that can be reset to starting point.
 * 
 * @author aas
 */
public interface IResetable {
    /**
     * Requests to reset the test to the starting point.
     * 
     * @param initiator the initiator of the reset.
     * @return true if action succeeded or false otherwise.
     */
    public boolean reset(IResetable initiator);
} // end interface IResetable
