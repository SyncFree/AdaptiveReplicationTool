package com.syncfree.adaptreplica.test;

/**
 * Support to restart the tests.
 * 
 * @author aas
 */
public interface ITestCtrler {
    /**
     * Supported actions.
     * 
     * @author aas
     */
    public enum ACTION {
        PLAY("Play"),
        NEXT("Next"),
        PAUSE("Pause"),
        STOP("Stop");

        private final String mstrTooltip;

        ACTION(final String strTooltip) {
            this.mstrTooltip = strTooltip;
        } // Constructor ()

        public final String getTooltip() {
            return this.mstrTooltip;
        } // getTooltip()
    }; // end enum ACTION

    /**
     * Called by the test runner (implementer of ITestRunner) to check if to
     * continue.
     * 
     * @param bAnyMore
     *            true if there is more tests to run or false otherwise.
     * @return true if to continue or false otherwise.
     */
    public boolean next(boolean bAnyMore);
    
    /**
     * @return the current state.
     */
    public ACTION getCurrentState();
    
    /**
     * Executes the specified test controller action.
     * 
     * @param action the action.
     */
    public void executeAction(ACTION action);
} // end class ITestCtrler
