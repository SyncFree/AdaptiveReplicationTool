package com.syncfree.adaptreplica.test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aasco.util.Helper;
import com.aasco.util.Verbose;
import com.syncfree.adaptreplica.controller.IController;
import com.syncfree.adaptreplica.controller.IFullController;
import com.syncfree.adaptreplica.controller.IMessageListener;
import com.syncfree.adaptreplica.controller.IResetable;
import com.syncfree.adaptreplica.controller.Message;
import com.syncfree.adaptreplica.controller.Message.TYPE;
import com.syncfree.adaptreplica.controller.ReadRequest;
import com.syncfree.adaptreplica.controller.Response;
import com.syncfree.adaptreplica.controller.WriteRequest;
import com.syncfree.adaptreplica.util.XmlHelper;

/**
 * Support to run sets of tests.
 * 
 * @author aas
 */
public class TestesController implements ITestsRunner {
    private static final int UNSET = -1;

    private IController mCtrl;
    private List<Tester> mTesters;
    private ITestCtrler mTestCtrler;

    public TestesController(final IController ctrl,
            final ITestCtrler testCtrler, final Element args) {
        this.mCtrl = ctrl;
        this.mTesters = new ArrayList<Tester>();

        this.mTestCtrler = testCtrler;
        for (int i = 0; i < args.getChildNodes().getLength(); ++i) {
            final Node node = args.getChildNodes().item(i);

            if (node instanceof Element && node.getNodeName().equals("test")) {
                final Element testerArg = (Element) node;
                final String strActive = testerArg.getAttribute("active");

                if (strActive == null || strActive.length() == 0
                        || Boolean.parseBoolean(strActive.toLowerCase())) {
                    this.mTesters.add(new Tester(testerArg));
                }
            }
        } // end for
    } // Constructor ()

    public ITestCtrler getTestCtrler() {
        return this.mTestCtrler;
    } // mTestCtrler()

    @Override
    public boolean reset(final IResetable initiator) {
        final boolean bContinue;

        Verbose.finer("Reseting the system...");

        for (final Tester tester : this.mTesters) {
            tester.reset(this);
        } // end for
        bContinue = this.mCtrl.reset(this);

        Verbose.finer("The system was reset");

        return bContinue;
    } // reset()

    /**
     * Stops all the tests.
     */
    public void stop() {
        for (final Tester tester : this.mTesters) {
            tester.stop();
        } // end for
    } // start()

    /**
     * Support the execution of a set of tests.
     * 
     * @author aas
     */
    class Tester implements Runnable, IMessageListener, ITestsRunner {
        private final List<TimeTests> mTimeTests;
        private final String mstrID;
        private final int miNumRuns;
        private int miRun;
        private int miRequetsID;
        private boolean mbStop;
        private boolean mbReset;
        private Thread mThread;

        Tester(final Element args) {
            final Element requests = XmlHelper.getNode(args, "requests");
            final NodeList list;
            long lTime;
            String value;

            this.miRequetsID = 0;
            this.mbStop = true;
            this.mstrID = args.getAttribute("id");
            Verbose.info("Starting test set \"{0}\"", getID());

            value = requests.getAttribute("repeat");
            if (value != null) {
                this.miNumRuns = Integer.parseInt(value);
            } else {
                this.miNumRuns = 1;
            }

            this.mTimeTests = new ArrayList<>();
            list = requests.getChildNodes();
            lTime = 0;
            for (int i = 0; i < list.getLength(); ++i) {
                final Node node = list.item(i);

                if (node instanceof Element) {
                    final Element testArgs = (Element) node;
                    final Test test = new Test(testArgs);

                    add(lTime, test);
                    lTime += test.getTime();
                }
            } // end for
//            Collections.sort(this.mTimeTests);

            this.mThread = new Thread(this, getID());
            ((IFullController) TestesController.this.mCtrl).register(this);

            Verbose.info("Test set \"{0}\" started", getID());
        } // Constructor ()

        /**
         * @return the ID of the set of tests.
         */
        public String getID() {
            return this.mstrID;
        } // getID()

        @Override
        public boolean reset(final IResetable initiator) {
            this.miRun = 0;
            this.mbReset = true;

            return true;
        } // reset()

        @Override
        public void run() {
            synchronized (this) {
                if (!this.mbStop) {
                    return;
                }
                this.mbStop = false;
            } // end synchronized

            long time;

            do {
                this.miRun = 0;
                OUTER: while (++this.miRun <= this.miNumRuns) {
                    Verbose.finer("Start set {0}, {1} out of {2,number,####}",
                            getID(), this.miRun, this.miNumRuns);

                    this.mbReset = false;
                    for (final TimeTests tTests : this.mTimeTests) {
                        if (this.mbStop || this.mbReset) {
                            break OUTER;
                        }

                        time = tTests.getTime();
                        if (time > 0) {
                            try {
                                long lRuns = time / 1000;
                                final long lRemainder;

                                while (!this.mbStop && !this.mbReset
                                        && --lRuns >= 0) {
                                    Thread.sleep(1000); // a second
                                } // end while
                                if (this.mbStop) {
                                    break OUTER;
                                } else if (this.mbReset) {
                                    break;
                                }

                                lRemainder = time % 1000;
                                if (lRemainder > 0) {
                                    Thread.sleep(lRemainder);
                                    if (this.mbStop) {
                                        break OUTER;
                                    } else if (this.mbReset) {
                                        break;
                                    }
                                }
                            } catch (InterruptedException ie) {
                                Verbose.log(Level.SEVERE, ie,
                                        "Stopping {0}; {1}", getID(),
                                        ie.getMessage());
                                break OUTER;
                            }
                        }

                        if (!TestesController.this.mTestCtrler.next(true)) {
                            break;
                        }

                        if (this.mbStop) {
                            break OUTER;
                        } else if (this.mbReset) {
                            break;
                        }
                        tTests.run();
                    } // end for
                } // end while
            } while (TestesController.this.mTestCtrler.next(false));
        } // run()

        @Override
        public void send(final Message<?> msg) {
            final Response<?> response = (Response<?>) msg;

            if (response.getRequestType() == TYPE.READ) {
                Verbose.info(
                        "{0} response to {1} from {2} with ID {3,number,####} value \"{4}\"",
                        response.getRequestType(), getID(), msg.getSenderID(),
                        msg.getID(), msg.getValue());
            } else {
                Verbose.info(
                        "{0} response to {1} from {2} with ID {3,number,####}",
                        response.getRequestType(), getID(), msg.getSenderID(),
                        msg.getID());
            }
        } // send()

        @Override
        public void setTestsCtrler(final ITestCtrler testCtrler) {
        } // setTestsCtrler()

        @Override
        public synchronized void start() {
            if (this.mbStop) {
                this.mThread.start();
            }
        } // start()

        /**
         * Stops this tester.
         */
        public synchronized void stop() {
            this.mbStop = true;
        } // stop()

        @Override
        public String toString() {
            return this.mstrID + ": num runs = " + this.miNumRuns + ", stop = "
                    + this.mbStop;
        } // toString()

        /**
         * Adds the specified test to this set of tests.
         * 
         * @param lTimeFromStartSet
         *            the time from the start of the set.
         * @param test
         *            the test.
         */
        protected void add(final long lTimeFromStartSet, final Test test) {
            if (test.getTime() == 0 && this.mTimeTests.size() > 0) {
                final TimeTests tTests = this.mTimeTests.get(this.mTimeTests
                        .size() - 1);

                tTests.add(test);

                return;
            }

            final TimeTests tTests = new TimeTests(lTimeFromStartSet,
                    test.getTime());

            tTests.add(test);
            this.mTimeTests.add(tTests);
        } // add()

        /**
         * Holds all the tests to be run at an specified time.
         * 
         * @author aas
         */
        class TimeTests implements Comparable<TimeTests> {
            private final long mlTimeFromStartSet;
            private final long mlTime;
            private final List<Test> mTests;
            private boolean mbNotChangedState;

            TimeTests(final long lTimeFromStartSet, final long time) {
                this.mTests = new ArrayList<>();
                this.mlTimeFromStartSet = lTimeFromStartSet;
                this.mlTime = time;
            } // Constructor ()

            public long getTimeFromStartSet() {
                return this.mlTimeFromStartSet;
            } // getTimeFromStartSet()

            public long getTime() {
                return this.mlTime;
            } // getTime()

            public void add(final Test test) {
                this.mTests.add(test);
            } // add()

            /**
             * Runs all the test associated to this time.
             */
            public void run() {
                this.mbNotChangedState = true;
                for (final Test test : this.mTests) {
                    if (Tester.this.mbStop || Tester.this.mbReset) {
                        break;
                    }

                    if (this.mbNotChangedState || TestesController.this.mTestCtrler.next(true)) {
                        this.mbNotChangedState = test.run();
                    } else {
                        break;
                    }
                } // end for
            } // run()

            @Override
            public int compareTo(final TimeTests tTest) {
                if (this.mlTime > tTest.mlTime) {
                    return 1;
                } else if (this.mlTime < tTest.mlTime) {
                    return -1;
                }

                return 0;
            } // compareTo()

            @Override
            public String toString() {
                return "Tests at " + getTime() + "msec";
            } // toString()
        } // end class TimeTests

        /**
         * A test.
         * 
         * @author aas
         */
        class Test {
            private final int miID;
            private final long mTime;
            private final TYPE mType;
            private final String mstrDestination;
            private final Object mValue;
            private final ITestCtrler.ACTION mAction;
            private final int miRepeated;

            Test(final Element args) {
                final String strDataType = args.getAttribute("data_type");
                String strData;

                strData = args.getAttribute("id");
                if (strData == null || strData.length() == 0) {
                    this.miID = ++Tester.this.miRequetsID;
                } else {
                    this.miID = Integer.parseInt(strData);
                }

                strData = args.getAttribute("offset_msec");
                if (strData.indexOf(':') == -1) {
                    this.mTime = Integer.parseInt(strData);
                } else {
                    this.mTime = Helper.getTimeLong(strData);
                }
                this.mType = TYPE.valueOf(args.getAttribute("type")
                        .toUpperCase());
                if (this.mType == TYPE.WRITE) {
                    strData = args.getLastChild().getTextContent().trim();
                    if (strDataType.equals("int")) {
                        this.mValue = Integer.parseInt(strData);
                    } else if (strDataType.equals("long")) {
                        this.mValue = Long.parseLong(strData);
                    } else if (strDataType.equals("double")) {
                        this.mValue = Double.parseDouble(strData);
                    } else {
                        this.mValue = strData;
                    }
                } else {
                    this.mValue = null;
                }
                this.mstrDestination = args.getAttribute("destination");

                strData = args.getAttribute("action").toUpperCase();
                if (strData == null || strData.length() == 0) {
                    this.mAction = null;
                    this.miRepeated = UNSET;
                } else {
                    this.mAction = ITestCtrler.ACTION.valueOf(strData);
                    strData = args.getAttribute("repeated").toUpperCase();
                    if (strData == null) {
                        this.miRepeated = UNSET;
                    } else {
                        this.miRepeated = Integer.valueOf(strData);
                    }
                }
            } // Constructor ()

            /**
             * @return the offset time the test should be run
             */
            public long getTime() {
                return this.mTime;
            } // getTime()

            /**
             * Sends this message.
             * 
             * @return true for unrestricted continuation or false otherwise.
             */
            public boolean run() {
                final Message<?> msg;

                if (this.mType == TYPE.READ) {
                    msg = new ReadRequest<>(Tester.this.getID(), this.miID);
                    Verbose.info(
                            "Request {3} from {0} to {1} with ID {2,number,####}",
                            Tester.this.getID(),
                            this.mstrDestination,
                            this.miID,
                            this.mType);
                } else {
                    msg = new WriteRequest<>(Tester.this.getID(), this.miID,
                            this.mValue);
                    Verbose.info(
                            "Request {4} from {0} to {1} with ID {2,number,####} and value \"{3}\"",
                            Tester.this.getID(),
                            this.mstrDestination,
                            this.miID,
                            msg.getValue(),
                            this.mType);
                }
                TestesController.this.mCtrl.send(this.mstrDestination, msg);

                if (this.mAction != null
                        && (this.miRepeated == UNSET || this.miRepeated == Tester.this.miRun)) {
                    TestesController.this.mTestCtrler
                            .executeAction(this.mAction);

                    return false;
                }
                
                return true;
            } // run()

            @Override
            public String toString() {
                if (this.mType == TYPE.READ) {
                    return this.miID + ": <= " + this.mstrDestination;
                }

                return this.miID + ": " + this.mValue + " => "
                        + this.mstrDestination;
            } // toString()
        } // end class Test
    } // end class TimeTests
} // end class TestesController
