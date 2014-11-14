package com.syncfree.adaptreplica.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.algorithm.IAlgorithm;
import com.syncfree.adaptreplica.algorithm.IAlgorithmListener;
import com.syncfree.adaptreplica.controller.Message.TYPE;
import com.syncfree.adaptreplica.test.ITestCtrler;
import com.syncfree.adaptreplica.util.XmlHelper;

/**
 * Representation of a data centre.
 * 
 * @author aas
 */
public class DataCentre implements IProcessor, IMessageListener, IDataHolder {
    private static final Controller msController = new Controller();

    public static final String LISTENER_GUI = "GUI";

    private static final String NODE_ALG = "algorithm";
    private static final String NODE_REPLICA = "replicate";

    private static final String ATTR_ID = "id";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_CLOSEST = "closest"; // part of
                                                          // NODE_REPLICA

    private static final int DEFAULT_MIN = 1;

    private final String mstrID;
    private final Processor mMsgSupport;
    private final IAlgorithm mAlgorithm;
    private final int miMinNumReplicas;
    private final Set<DataCentre> mDcReplicas;
    private final List<RequestHolder> mRedirected;
    private final InitialValues mOriginalValues;
    private Object mData;
    private DataCentre mClosest;
    private IAlgorithmListener mGuiListener;

    public DataCentre(final Element element, final IAlgorithm defaultAlg)
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Node node;

        this.mstrID = element.getAttribute(ATTR_ID);
        this.mMsgSupport = new Processor(this);
        this.mDcReplicas = new HashSet<DataCentre>();
        this.mRedirected = new ArrayList<RequestHolder>();
        this.mOriginalValues = new InitialValues(element);
        node = XmlHelper.getNode(element.getParentNode().getParentNode(),
                "default_dc");
        if (node == null) {
            this.miMinNumReplicas = DEFAULT_MIN;
        } else {
            this.miMinNumReplicas = XmlHelper.getValue(node, "name",
                    "minNumReplicas", DEFAULT_MIN);
        }
        this.mAlgorithm = buildAlgorithm(element, defaultAlg);

        add(this);
    } // Constructor ()

    public int getNumDataCentres() {
        return (this.mRedirected.size() - 1);
    } // getNumDataCentres()

    @Override
    public void onStarting() {
    } // onStarting()

    @Override
    public void onStopping() {
        this.mClosest = null;
        // this.mMsg = null;
        this.mDcReplicas.clear();
        this.mRedirected.clear();
    } // onStopping()

    /**
     * @return the data centre ID.
     */
    public String getID() {
        return this.mstrID;
    } // getID()

    /**
     * @return the overall controller.
     */
    public static Controller getController() {
        return DataCentre.msController;
    } // getController()

    /**
     * Updates the internal list of DCs if this has a replica.
     */
    public void initialise(final Collection<IMessageListener> values,
            final IAlgorithmListener algListener) {
        this.mOriginalValues.update(values);
        if (algListener != null) {
            this.mAlgorithm.setListener(algListener);
        }
        if (!isReplicated()) {
            return;
        }

        for (final IMessageListener listener : values) {
            if (listener instanceof DataCentre && listener != this) {
                final DataCentre dc = (DataCentre) listener;

                if (dc.isReplicated()) {
                    addDC(dc);
                    this.mClosest = null;
                }
            } else if (this.mGuiListener == null
                    && listener instanceof IAlgorithmListener
                    && listener.getID().equals(LISTENER_GUI)) {
                this.mGuiListener = (IAlgorithmListener) listener;
            }
        } // end for
    } // initialise()

    /**
     * Starts all the Data Centres.
     */
    public static void startAll() {
        DataCentre.msController.startAll();
    } // startAll()

    /**
     * Stops all the Data Centres.
     */
    public static void stopDCs() {
        final int iId = -1;
        final Message<?> msg = new Message<>(null, iId, TYPE.STOP, null);

        DataCentre.msController.sendAll(msg);
    } // stopDCs()

    @Override
    public void send(Message<?> message) {
        this.mMsgSupport.send(message);
    } // send()

    @Override
    public Object getData() {
        return this.mData;
    } // getData()

    public boolean isReplicated() {
        return (this.mData != null);
    } // isReplicated()

    @Override
    public void setData(final Object data) {
        this.mData = data;
        if (this.mGuiListener != null) {
            this.mGuiListener.onChange(this, this.mAlgorithm.getLevel());
        }
    } // setData()

    /**
     * Builds an algorithm from the provided parameters.
     * 
     * @param algArgs
     *            the parameters for the algorithm.
     * @return the built algorithm.
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static IAlgorithm buildAlgorithm(final Element algArgs)
            throws ClassNotFoundException, NoSuchMethodException,
            SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final String className = algArgs.getAttribute(ATTR_CLASS);
        final Class<?>[] aClazzes = { Element.class };
        final Object[] aArgs = { algArgs };
        final Class<?> clazz = Class.forName(className);
        final Constructor<?> constructor = clazz.getConstructor(aClazzes);

        return (IAlgorithm) constructor.newInstance(aArgs);
    } // buildAlgorithm()

    @Override
    public void start() {
        this.mMsgSupport.start();
        this.mAlgorithm.start();
    } // start()

    @Override
    public void stop() {
        this.mMsgSupport.stop();
        this.mAlgorithm.stop();
    } // stop()

    @Override
    public boolean process(final Message<?> msg) {
        switch (msg.getType()) {
        case STOP:
            DataCentre.msController.unregister(this);
            return true;

        case READ:
            read(msg);
            break;

        case READ_NEW_REPLICA:
            if (isReplicated()) {
                addDC((DataCentre) DataCentre.msController.get(msg
                        .getSenderID()));
                this.mClosest = null;

                reply(msg.getSenderID(), this.mMsgSupport.getNextMsgID(),
                        msg.getType(), null);
            }
            break;

        case WRITE:
            write(msg);
            break;

        case WRITE_NEW_REPLICA:
            writeNew(msg);
            break;

        case REMOVED_REPLICA:
            Verbose.fine("Removing {0} from replica list in {1}",
                    msg.getSenderID(), getID());
            for (final DataCentre dc : this.mDcReplicas) {
                if (dc.getID().equals(msg.getSenderID())) {
                    this.mDcReplicas.remove(dc);

                    break;
                }
            } // end for
            Verbose.fine("Removed {0} from replica list in {1}",
                    msg.getSenderID(), getID());
            break;

        case HAS_REPLICA:
            hasReplica(msg);
            break;

        case RESPONSE:
            response(msg);
            break;

        case ALGORITHM:
            if (!this.mAlgorithm.apply(msg) && couldReduceReplicas()) {
                removeReplica();
            }
            break;

        default:
        } // end switch

        return false;
    } // process()

    @Override
    public String toString() {
        return getID();
    } // toString()

    /**
     * @return true if replicas may be reduced or false otherwise.
     */
    public boolean couldReduceReplicas() {
        return (this.mDcReplicas.size() >= this.miMinNumReplicas);
    } // couldReduceReplicas()

    public boolean reset(final Collection<IMessageListener> values) {
        this.mOriginalValues.update(values);
        this.mRedirected.clear();
        this.mDcReplicas.clear();
        this.mAlgorithm.reset(null);

        return true;
    } // reset()

    /**
     * Builds and sends an algorithm request to itself with the specified value.
     * 
     * @param value
     *            the value.
     */
    public <T> void sendAlgorithmRequest(final T value) {
        send(new AlgorithmRequest<>(getID(), this.mMsgSupport.getNextMsgID(),
                value));
    } // sendAlgorithmRequest()

    @Override
    public void setTestsCtrler(ITestCtrler testCtrler) {
        this.mAlgorithm.setTestsCtrler(testCtrler);
    } // setTestsCtrler()

    protected IAlgorithm buildAlgorithm(final Element element,
            final IAlgorithm defaultAlg) throws ClassNotFoundException,
            NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        final Element algArgs = XmlHelper.getNode(element.getChildNodes(),
                NODE_ALG);

        if (algArgs != null && algArgs.getAttribute("class").length() > 0) {
            return buildAlgorithm(algArgs);
        }

        return defaultAlg.clone(algArgs, this);
    } // buildAlgorithm()

    /**
     * Processes the has replica message.
     * 
     * @param msg
     *            the message.
     */
    protected void hasReplica(final Message<?> msg) {
        if (isReplicated()) {
            reply(msg.getSenderID(), this.mMsgSupport.getNextMsgID(),
                    TYPE.HAS_REPLICA, null);
        }
    } // hasReplica()

    /**
     * Processes the read message.
     * 
     * @param msg
     *            the message.
     */
    protected void read(final Message<?> msg) {
        final Request<?> request = (Request<?>) msg;
        final IMessageListener sender = DataCentre.msController.get(request
                .getSenderID());

        if (sender instanceof DataCentre) {
            // Redirection from another DC
            this.mAlgorithm.otherRead((DataCentre) sender);

            // Notify requesting DC
        } else {
            final boolean noData = (this.mData == null);

            if (noData) {
                setClosest(msg);

                return;
            }
            this.mAlgorithm.ownRead();
        }
        reply(request.getSenderID(), request.getID(), request.getType(),
                this.mData);
    } // read()

    /**
     * Processes the response provided.
     * 
     * @param msg
     *            the response.
     */
    protected void response(final Message<?> msg) {
        final Response<?> response = (Response<?>) msg;

        switch (response.getRequestType()) {
        case READ_NEW_REPLICA:
            addDC((DataCentre) DataCentre.msController.get(msg.getSenderID()));
            break;

        case HAS_REPLICA:
            if (!isReplicated()) {
                boolean bSet = false;
                boolean bSending = true;
                Object data = null;

                // Check if current closest DC is further than this
                setCloserDC(response.getSenderID());

                // Continue processing request. Give preference to write
                for (int i = 0; i < this.mRedirected.size(); ++i) {
                    final RequestHolder request = this.mRedirected.get(i);

                    if (!request.mSent
                            && request.getRequest().getType() == TYPE.WRITE) {
                        final WriteRequest<?> forward = new WriteRequest<>(
                                getID(), this.mMsgSupport.getNextMsgID(),
                                new DataNoReplicaWrite<>(isReplicated(),
                                        request.getRequest().getValue()));

                        this.mClosest.send(forward);
                        request.setSent();
                        data = request.getRequest().getValue();
                        bSet = true;
                    }
                } // end for

                for (int i = 0; i < this.mRedirected.size(); ++i) {
                    final RequestHolder request = this.mRedirected.get(i);

                    if (!request.mSent
                            && request.getRequest().getType() == TYPE.READ) {
                        if (bSet) {
                            // The data will be changed so use latest changes
                            reply(request.getRequest().getSenderID(), request
                                    .getRequest().getID(), request.getRequest()
                                    .getType(), data);

                            // Do not request read again
                            this.mRedirected.remove(i);
                            --i;
                        } else {
                            if (bSending) {
                                final ForwardRequest<?> forward = new ForwardRequest<>(
                                        getID(),
                                        this.mMsgSupport.getNextMsgID(),
                                        request.getRequest());

                                this.mClosest.send(forward);

                                // Forward the request just once
                                bSending = false;
                            }
                            request.setSent();
                        }
                    }
                } // end for
            }
            break;

        case READ: {
            // Received response from a read redirection
            boolean bNotUpadted = true;

            this.mAlgorithm.ownRead();

            // Respond to all outstanding read requests
            for (int i = 0; i < this.mRedirected.size(); ++i) {
                final Request<?> request = this.mRedirected.get(i).getRequest();

                if (request instanceof ReadRequest) {
                    this.mRedirected.remove(i);
                    --i;
                    if (bNotUpadted && this.mAlgorithm.isReplicated()) {
                        // Update the value
                        final boolean newReplica = !isReplicated();

                        bNotUpadted = false;
                        setData(response.getValue());
                        this.mAlgorithm.dataWasSet();
                        if (newReplica) {
                            DataCentre.msController.sendAll(
                                    new NewReadReplicaNotification(getID(),
                                            request.getID()), getID());
                        }
                    }
                    reply(request.getSenderID(), request.getID(),
                            request.getType(), response.getValue());
                }
            } // end for
        }
            break;

        case WRITE:
            if (isReplicated()) {
                // A write in a DC without replica
                final DataCDIDs<?> data = (DataCDIDs<?>) response.getValue();
                final NewWriteReplicaNotification<?> forward = new NewWriteReplicaNotification<>(
                        getID(), this.mMsgSupport.getNextMsgID(), data.mValue);
                final String[] astrDCNames = data.mstrDCIDs.toString().split(
                        ",");

                this.mClosest = null;
                for (final String strDCName : astrDCNames) {
                    final DataCentre dc = (DataCentre) DataCentre.msController
                            .get(strDCName);

                    if (dc != null) {
                        addDC(dc);
                        dc.send(forward);
                    }
                } // end for
            }
            break;

        default:
        } // end switch
    } // response()

    /**
     * Adds the specified DC to the list of replicas.
     * 
     * @param dc
     *            the new Data Centre with a replica.
     */
    protected final void addDC(final DataCentre dc) {
        Verbose.fine("Adding {0} into replica list in {1}", dc.getID(), getID());
        this.mDcReplicas.add(dc);
        Verbose.fine("Added {0} into replica list in {1}", dc.getID(), getID());
    } // addDC()

    /**
     * Processes a read request from what it will be a new replica.
     * 
     * @param msg
     *            the message.
     */
    protected void writeNew(final Message<?> msg) {
        //
        final IMessageListener sender = DataCentre.msController.get(msg
                .getSenderID());
        final NewWriteReplicaNotification<?> request = (NewWriteReplicaNotification<?>) msg;
        final Object value;

        if (request.getValue() instanceof DataNoReplicaWrite) {
            value = ((DataNoReplicaWrite<?>) request.getValue()).mValue;
        } else {
            value = request.getValue();
        }

        addDC((DataCentre) sender);
        if (this.mAlgorithm.otherWrite((DataCentre) sender, value)
                || !couldReduceReplicas()) {
            setData(value);
        } else {
            removeReplica();
        }
    } // writeNew()

    /**
     * Processes the write for the provided message.
     * 
     * @param msg
     *            the message.
     */
    protected void write(final Message<?> msg) {
        final IMessageListener sender = DataCentre.msController.get(msg
                .getSenderID());
        final WriteRequest<?> request = (WriteRequest<?>) msg;
        final IMessageListener listener = DataCentre.msController.get(msg
                .getSenderID());

        if (listener instanceof DataCentre) {
            final Object value;

            if (!isReplicated()) {
                // Ignore
                Verbose.finer(
                        "{0} request with ID {1} from {2} was recived but {3} does not have a replica; ignoring request",
                        msg.getType(), msg.getID(), msg.getSenderID(), getID());

                return;
            }

            if (request.getValue() instanceof DataNoReplicaWrite) {
                final DataNoReplicaWrite<?> data = (DataNoReplicaWrite<?>) request
                        .getValue();
                final WriteRequest<?> forward = new WriteRequest<>(getID(),
                        this.mMsgSupport.getNextMsgID(), data.mValue);

                for (final DataCentre dc : this.mDcReplicas) {
                    dc.send(forward);
                } // end for

                value = data.mValue;
                if (data.mbReplicated) {
                    replayWithDCWithReplicas(request);
                }
            } else {
                value = request.getValue();
            }

            if (this.mAlgorithm.otherWrite((DataCentre) sender, value)
                    || !couldReduceReplicas()) {
                setData(value);
            } else {
                // The data does not exist in this DC anymore
                removeReplica();

                return;
            }
        } else {
            final boolean isNotReplicated = !isReplicated();

            if (this.mAlgorithm.ownWrite(request.getValue())) {
                setData(request.getValue());
                if (isNotReplicated) {
                    request.setType(TYPE.WRITE_NEW_REPLICA);
                }
            }

            if (isNotReplicated) {
                if (this.mClosest == null) {
                    // Find closest
                    setClosest(request);

                    return;
                } else {
                    // Now all are sent to other DCs but they could wait and
                    // be sent all in one later. The problem is that a
                    // replicate maybe should not exist anymore but it is
                    // kept until it is fully updated
                    final WriteRequest<?> forward = new WriteRequest<>(getID(),
                            this.mMsgSupport.getNextMsgID(),
                            new DataNoReplicaWrite<>(
                                    this.mAlgorithm.isReplicated(),
                                    request.getValue()));

                    this.mClosest.send(forward);
                }
            } else {
                // Now all are sent to other DCs but they could wait and be
                // sent all in one later. The problem is that a replicate
                // maybe should not exist anymore but it is kept until it is
                // fully updated
                final WriteRequest<?> forward = new WriteRequest<>(getID(),
                        this.mMsgSupport.getNextMsgID(), request.getValue());

                for (final DataCentre dc : this.mDcReplicas) {
                    dc.send(forward);
                } // end for
            }
            reply(request.getSenderID(), request.getID(), request.getType(),
                    null);
        }
    } // write()

    protected void replayWithDCWithReplicas(final Request<?> request) {
        final StringBuilder value = new StringBuilder();

        value.append(getID());
        for (final DataCentre dc : this.mDcReplicas) {
            value.append(',');
            value.append(dc.getID());
        } // end for
        reply(request.getSenderID(), request.getID(), request.getType(),
                new DataCDIDs<>(value.toString(), request.getValue()));
    } // replayWithDCWithReplicas()

    /**
     * Removes the replica from this DC and notify all the others.
     */
    protected void removeReplica() {
        if (!couldReduceReplicas()) {
            return;
        }

        Verbose.info("Removing replica from {0}", getID());

        // The data does not exist in this DC anymore
        setData(null);
        this.mClosest = null;
        this.mAlgorithm.updateLevel();

        // Notify of the removal of the replica
        final RemoveReplicaRequest request = new RemoveReplicaRequest(getID(),
                this.mMsgSupport.getNextMsgID());

        for (final DataCentre dc : this.mDcReplicas) {
            dc.send(request);
        } // end for
        this.mDcReplicas.clear();

        // Stop processing any redirection
        this.mRedirected.clear();

        Verbose.info("Replica was removed from {0}", getID());
    } // removeReplica()

    /**
     * Adds the specified DC into the static list of data centres.
     * 
     * @param dc
     *            a new data centre.
     */
    private static void add(final DataCentre dc) {
        DataCentre.msController.register(dc);
    } // add()

    /**
     * Sets the passed data centre as closest if valid. The first that reply it
     * is the one used as closest.
     * 
     * @param strDC
     *            the ID for the Data Centre.
     */
    private void setCloserDC(final String strDC) {
        if (this.mClosest == null) {
            this.mClosest = (DataCentre) DataCentre.msController.get(strDC);
        } else {
            // Keep the first that replied
        }
    } // setCloserDC();

    /**
     * Builds reply for the specified data centre with the specified components.
     * 
     * @param strDestinationID
     *            the destination ID.
     * @param iId
     *            the message ID.
     * @param type
     *            the type of reply.
     * @param message
     *            the message.
     */
    private void reply(final String strDestinationID, final int iId,
            final TYPE type, final Object message) {
        final IMessageListener dc = DataCentre.msController
                .get(strDestinationID);

        dc.send(new Response<Object>(getID(), iId, type, message));
    } // reply()

    /**
     * Sets the data centre to be considered the closest.
     * 
     * @param msg
     *            the message.
     */
    private void setClosest(final Message<?> msg) {
        final HasReplicaRequest m = new HasReplicaRequest(getID(),
                this.mMsgSupport.getNextMsgID());

        if (this.mRedirected.size() > 0 && msg instanceof ReadRequest) {
            // Check that a read was not already sent
            for (final RequestHolder request : this.mRedirected) {
                if (request.getRequest() instanceof ReadRequest) {
                    this.mRedirected.add(new RequestHolder((Request<?>) msg));

                    return;
                }
            } // end for
        }

        this.mRedirected.add(new RequestHolder((Request<?>) msg));
        if (this.mClosest == null) {
            DataCentre.msController.sendAll(m, getID());
        } else {
            this.mClosest.send(m);
        }
    } // setClosest()

    /**
     * Holds DC IDs and value.
     * 
     * @author aas
     */
    class DataCDIDs<T> {
        public final String mstrDCIDs;
        public final T mValue;

        public DataCDIDs(final String strDCIDs, final T value) {
            this.mstrDCIDs = strDCIDs;
            this.mValue = value;
        } // Constructor ()
    } // end class DataCDIDs

    /**
     * Holds the replication state of the DC sending the write and the value to
     * write.
     * 
     * @author aas
     */
    class DataNoReplicaWrite<T> {
        public final boolean mbReplicated;
        public final T mValue;

        public DataNoReplicaWrite(final boolean bReplicated, final T value) {
            this.mbReplicated = bReplicated;
            this.mValue = value;
        } // Constructor ()
    } // end class DataNoReplicaWrite

    class InitialValues {
        private final Object mData;
        private final String mstrClosestDC;

        InitialValues(final Element element) {
            String strClosestDC = null;
            Node node;

            node = XmlHelper.getNode(element, NODE_REPLICA);
            if (node != null) {
                this.mData = ((Element) node).getLastChild().getTextContent()
                        .trim();
                DataCentre.this.setData(this.mData);
                strClosestDC = ((Element) node).getAttribute(ATTR_CLOSEST);
                if (strClosestDC != null && strClosestDC.length() == 0) {
                    strClosestDC = null;
                }
            } else {
                this.mData = null;
            }
            this.mstrClosestDC = strClosestDC;
        } // Constructor ()

        public void update(final Collection<IMessageListener> values) {
            DataCentre.this.setData(this.mData);
            if (this.mstrClosestDC != null) {
                for (final IMessageListener listener : values) {
                    if (listener instanceof DataCentre
                            && listener.getID().equals(this.mstrClosestDC)) {
                        DataCentre.this.mClosest = (DataCentre) listener;

                        break;
                    }
                } // end for
            }
        } // update()
    } // end class InitialValues

    /**
     * Hold information about a request forwarded.
     * 
     * @author aas
     */
    class RequestHolder {
        private final Request<?> mRequest;
        private boolean mSent;

        RequestHolder(final Request<?> request) {
            this.mRequest = request;
            this.mSent = false;
            for (final RequestHolder holder : DataCentre.this.mRedirected) {
                if (holder.getRequest().getType() == request.getType()
                        && holder.wasSent()) {
                    this.mSent = true;

                    break;
                }
            } // end for
        } // constructor ()

        public boolean wasSent() {
            return this.mSent;
        } // wasSent()

        public void setSent() {
            this.mSent = true;
        } // setSent()

        public Request<?> getRequest() {
            return this.mRequest;
        } // getRequest()
    } // end class RequestHolder
} // end class DataCentre
