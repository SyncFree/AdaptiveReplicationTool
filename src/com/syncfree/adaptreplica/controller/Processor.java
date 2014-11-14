package com.syncfree.adaptreplica.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.controller.Message.TYPE;

/**
 * Support for the queue and initial process of the messages.
 * 
 * @author aas
 */
public class Processor implements Runnable {
    private final BlockingQueue<Message<?>> mQueue;
    private final IProcessor mProcessor;
    private Thread mThread;
    private boolean mbRunning;
    private int miMsgID;

    public Processor(final IProcessor processor) {
        this.mProcessor = processor;
        this.mQueue = new LinkedBlockingQueue<Message<?>>();
        this.miMsgID = -1;
    } // Constructor ()

    /**
     * @return the ID of the processor.
     */
    public String getID() {
        return this.mProcessor.getID();
    } // getID()

    /**
     * If used outside the Processor thread synchronise it.
     * 
     * @return the ID for a new message.
     */
    public int getNextMsgID() {
        return ++this.miMsgID;
    } // getNextMsgID()

    /**
     * @return the ID of the last message.
     */
    public int getMsgID() {
        return this.miMsgID;
    } // getMsgID()

    /**
     * Processes the messages posted into the threads's queue.
     */
    @Override
    public void run() {
        Message<?> msg;

        if (!isRunning() || this.mbRunning) {
            return;
        }

        this.mbRunning = true;
        try {
            OUTER: while ((msg = this.mQueue.take()) != null) {
                switch (msg.getType()) {
                case STOP:
                    Verbose.finer(
                            "{3} is stopping as requested by message with ID {0,number,####} and type \"{1}\" from {2}",
                            msg.getID(), msg.getType(), msg.getSenderID(),
                            getID());
                    this.mProcessor.onStopping();
                    this.mQueue.clear();
                    Verbose.finer(
                            "{3} is stopped as requested by message with ID {0,####} and type \"{1}\" from {2}",
                            msg.getID(), msg.getType(), msg.getSenderID(),
                            getID());
                    break OUTER;

                default: {
                    final boolean bStop;

                    Verbose.finer(
                            "{3} is processing message with ID {0} and type \"{1}\" from {2}",
                            msg.getID(), msg.getType(), msg.getSenderID(),
                            getID());
                    bStop = this.mProcessor.process(msg);
                    Verbose.finer(
                            "{3} processed message with ID {0} and type \"{1}\" from {2}",
                            msg.getID(), msg.getType(), msg.getSenderID(),
                            getID());
                    if (bStop) {
                        this.mQueue.clear();
                        Verbose.finer(
                                "{3} is stopped as consequence by message with ID {0} and type \"{1}\" from {2}",
                                msg.getID(), msg.getType(), msg.getSenderID(),
                                getID());

                        break OUTER;
                    }
                }
                } // end switch
            } // end while
        } catch (final InterruptedException ie) {
            Verbose.log(Level.SEVERE, ie, "Thread: {0}", ie.getMessage());
            this.mQueue.clear();
        } // end try

        this.mThread = null;
        this.mbRunning = false;
        Verbose.info("{0} has stopped", getID());
    } // run()

    public final boolean isRunning() {
        return (this.mThread != null);
    } // isRunning()

    /**
     * Sends a starting message to this processor to start.
     */
    public synchronized void start() {
        if (isRunning()) {
            Verbose.warning("{0} is already running", getID());
        } else {
            this.mbRunning = false;
            this.mThread = new Thread(this, getID());
            this.mProcessor.onStarting();
            this.mThread.start();

            Verbose.info("{0} was started", getID());
        }
    } // start()

    /**
     * Sends a stopping message to this processor to stop.
     */
    public void stop() {
        send(new Message<>(getID(), getNextMsgID(), TYPE.STOP, null));
    } // stop()

    /**
     * Add the specified message to the queue to be processed.
     * 
     * @param msg
     *            the message.
     */
    public void send(final Message<?> msg) {
        this.mQueue.offer(msg);
    } // send()
} // end class Processor
