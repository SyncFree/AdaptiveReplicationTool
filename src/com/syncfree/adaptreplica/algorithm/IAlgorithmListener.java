package com.syncfree.adaptreplica.algorithm;

import com.syncfree.adaptreplica.controller.DataCentre;

/**
 * The listener interface for updating other components like the GUI.
 * 
 * @author aas
 */
public interface IAlgorithmListener {
    /**
     * Called when the level has changed.
     * 
     * @param dc the data centre.
     * @param iLevel the new level.
     */
    public void onChange(DataCentre dc, int iLevel);
} // end interface IAlgorithmListener
