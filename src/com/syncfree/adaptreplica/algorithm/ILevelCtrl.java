package com.syncfree.adaptreplica.algorithm;

import com.syncfree.adaptreplica.test.ITestCtrler;


/**
 * Interface to obtain current level.
 * 
 * @author aas
 */
public interface ILevelCtrl {
    /**
     * Sets the running controller.
     * 
     * @param testCtrler the running controller.
     */
    public void setTestsCtrler(final ITestCtrler testCtrler);

    /**
     * @return the level for the given strength.
     */
    public int getLevel();
    
    /**
     * States to the algorithm that recalculate the level.
     */
    public void updateLevel();
} // end interface ILevelCtrl
