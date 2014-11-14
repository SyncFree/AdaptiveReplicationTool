package com.syncfree.adaptreplica.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.syncfree.adaptreplica.algorithm.IAlgorithmListener;
import com.syncfree.adaptreplica.controller.Message.TYPE;
import com.syncfree.adaptreplica.test.ITestCtrler;

/**
 * Data storage of all Data Centres.
 * 
 * @author aas
 */
public class Controller implements IFullController {
    private final Map<String, IMessageListener> mDcs = new ConcurrentHashMap<String, IMessageListener>();
    private List<IMessageListener> mInitialiseListener = new ArrayList<>();
    private int iNumNoDCs;

    @Override
    public void setTestsCtrler(final ITestCtrler testCtrler) {
        for (final IMessageListener listener : this.mDcs.values()) {
            listener.setTestsCtrler(testCtrler);
        } // end for
    } // setTestsCtrler()

    /**
     * Calls the #initialise() for each of the DCs.
     */
    public void initialise(final IAlgorithmListener algListener) {
        for (IMessageListener listener : this.mDcs.values()) {
            if (listener instanceof DataCentre) {
                final DataCentre dc = (DataCentre) listener;

                dc.initialise(this.mDcs.values(), algListener);
            }
        } // end for
    } // initialise()

    @Override
    public boolean reset(final IResetable initiator) {
        final Collection<IMessageListener> listeners = this.mDcs.values();

        // 1st reset Data Centres
        for (IMessageListener listener : listeners) {
            if (listener instanceof DataCentre) {
                ((DataCentre) listener).reset(listeners);
            }
        } // end for

        // Needed to update replication map
        for (IMessageListener listener : listeners) {
            if (listener instanceof DataCentre) {
                ((DataCentre) listener).initialise(listeners, null);
            }
        } // end for

        // 2nd reset other resetables
        for (IMessageListener listener : listeners) {
            if (listener instanceof IResetable && listener != initiator) {
                ((IResetable) listener).reset(this);
            }
        } // end for

        return true;
    } // reset()

    /**
     * @return the number of data centre.
     */
    public int getNumDataCentres() {
        return (this.mDcs.size() - this.iNumNoDCs);
    } // getNumDataCentres()

    @Override
    public IMessageListener get(final String strID) {
        return this.mDcs.get(strID);
    } // get()

    @Override
    public void register(final IMessageListener listener) {
        this.mDcs.put(listener.getID(), listener);
        this.mInitialiseListener.add(listener);
        if (!(listener instanceof DataCentre)) {
            ++this.iNumNoDCs;
        }
    } // register()

    @Override
    public void unregister(final IMessageListener listener) {
        this.mDcs.remove(listener);
        this.mInitialiseListener.remove(listener);
    } // unregister()

    @Override
    public void reply(final String strCurrentID, final String strDestinationID,
            final int iId, final TYPE type, final Object message) {
        final IMessageListener dc = this.mDcs.get(strDestinationID);

        dc.send(new Response<Object>(strCurrentID, iId, type, message));
    } // reply()

    @Override
    public void send(final String destinamtionID, final Message<?> msg) {
        final IMessageListener ctrl = this.mDcs.get(destinamtionID);

        ctrl.send(msg);
    } // send()

    @Override
    public void sendAll(final Message<?> msg, final String... astrExceptDCs) {
        OUTER:
        for (final IMessageListener dc : this.mDcs.values()) {
            if (dc instanceof DataCentre) {
                for (final String strID : astrExceptDCs) {
                    if (dc.getID().equals(strID)) {
                        continue OUTER;
                    }
                } // end for
                dc.send(msg);
            }
        } // end for
    } // sendAll()

    /**
     * Starts all the processes.
     */
    public void startAll() {
        for (final IMessageListener dc : this.mInitialiseListener) {
            if (dc instanceof DataCentre) {
                dc.start();
            }
        } // end for
        Thread.yield();

        for (final IMessageListener listener : this.mInitialiseListener) {
            if (listener instanceof IAlgorithmListener) {
                listener.start();
            }
        } // end for
        Thread.yield();

        for (final IMessageListener other : this.mInitialiseListener) {
            if (!(other instanceof IProcessor) && !(other instanceof DataCentre)) {
                other.start();
            }
        } // end for
    } // startAll()
} // end class Controller
