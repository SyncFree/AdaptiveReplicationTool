package com.syncfree.adaptreplica.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper methods for XTML.
 * 
 * @author aas
 */
public class XmlHelper {
    // No instance allowed
    private XmlHelper() {
    } // Constructor ()

    public static String getArg(final Node node, final String strName,
            final String strDefaultValue) {
        return getArg(node.getChildNodes(), strName, strDefaultValue);
    } // getArg()

    public static String getArg(final NodeList args, final String strName,
            final String strDefaultValue) {
        for (int i = 0; i < args.getLength(); ++i) {
            final Node node = args.item(i);

            if (node instanceof Element) {
                final Element arg = (Element) node;

                if (arg.getAttribute("name").equals(strName)) {
                    return node.getLastChild().getTextContent().trim();
                }
            }
        } // end for

        return strDefaultValue;
    } // getArgs()

    public static String getArgAttr(final NodeList args, final String strName,
            final String strAttrName) {
        for (int i = 0; i < args.getLength(); ++i) {
            final Node n = args.item(i);

            if (n instanceof Element) {
                final Element arg = (Element) n;

                if (arg.getAttribute("name").equals(strName)) {
                    return ((Element) n).getAttribute(strAttrName);
                }
            }
        } // end for

        return null;
    } // getArgAttr()

    public static String getArgAttr(final Element node, final String strName,
            final String strAttrName) {
        return getArgAttr(node.getChildNodes(), strName, strAttrName);
    } // getArgAttr()

    public static double getArgAttr(final NodeList args, final String strName,
            final String strAttrName, final double dDefaultValue) {
        final String strValue = getArgAttr(args, strName, strAttrName);

        if (strValue == null) {
            return dDefaultValue;
        }

        return Double.parseDouble(strValue);
    } // getArgAttr()

    public static double getArgAttr(final Element node, final String strName,
            final String strAttrName, final double dDefaultValue) {
        final String strValue = getArgAttr(node, strName, strAttrName);

        if (strValue == null) {
            return dDefaultValue;
        }

        return Double.parseDouble(strValue);
    } // getArgAttr()

    public static int getArgAttr(final Element node, final String strName,
            final String strAttrName, final int iDefaultValue) {
        final String strValue = getArgAttr(node, strName, strAttrName);

        if (strValue == null) {
            return iDefaultValue;
        }

        return Integer.parseInt(strValue);
    } // getArgAttr()

    public static int getArgAttr(final NodeList arg, final String strName,
            final String strAttrName, final int iDefaultValue) {
        final String strValue = getArgAttr(arg, strName, strAttrName);

        if (strValue == null) {
            return iDefaultValue;
        }

        return Integer.parseInt(strValue);
    } // getArgAttr()

    public static double getArg(final Node node, final String strName,
            final double dDefaultValue) {
        return getArg(node.getChildNodes(), strName, dDefaultValue);
    } // getArg()

    public static double getArg(final NodeList args, final String strName,
            final double dDefaultValue) {
        final String strValue = getArg(args, strName, null);

        if (strValue == null) {
            return dDefaultValue;
        }

        return Double.valueOf(strValue);
    } // getArg()

    public static int getArg(final Node node, final String strName,
            final int iDefaultValue) {
        return getArg(node.getChildNodes(), strName, iDefaultValue);
    } // getArg()

    public static int getArg(final NodeList args, final String strName,
            final int iDefaultValue) {
        final String strValue = getArg(args, strName, null);

        if (strValue == null) {
            return iDefaultValue;
        }

        return Integer.valueOf(strValue);
    } // getArg()

    public static double getArgDouble(final NodeList args, final String strName) {
        final String strValue = getArg(args, strName, null);

        return Double.valueOf(strValue);
    } // getArgDouble()

    public static int getArgInteger(final NodeList args, final String strName) {
        final String strValue = getArg(args, strName, null);

        return Integer.valueOf(strValue);
    } // getArgInteger()

    public static Element getNode(final Node node, final String strTagName) {
        return getNode(node.getChildNodes(), strTagName);
    } // getNode()

    public static Element getNode(final NodeList nodes, final String strTagName) {
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Node node = nodes.item(i);

            if (node instanceof Element
                    && node.getNodeName().equals(strTagName)) {
                return (Element) node;
            }
        } // end for

        return null;
    } // getNode()

    public static String getNodeValue(final Node parentNode,
            final String strNodeName, final String strDefaultValue) {
        return getNodeValue(parentNode.getChildNodes(), strNodeName,
                strDefaultValue);
    } // getNodeValue()

    public static int getNodeValue(final Node parentNode,
            final String strNodeName, final int iDefaultValue) {
        final String strValue = getNodeValue(parentNode.getChildNodes(),
                strNodeName, null);

        if (strValue != null) {
            return Integer.parseInt(strValue);
        }

        return iDefaultValue;
    } // getNodeValue()

    public static long getNodeValue(final Node parentNode,
            final String strNodeName, final long lDefaultValue) {
        final String strValue = getNodeValue(parentNode.getChildNodes(),
                strNodeName, null);

        if (strValue != null) {
            return Long.parseLong(strValue);
        }

        return lDefaultValue;
    } // getNodeValue()

    public static double getNodeValue(final Node parentNode,
            final String strNodeName, final double dDefaultValue) {
        final String strValue = getNodeValue(parentNode.getChildNodes(),
                strNodeName, null);

        if (strValue != null) {
            return Double.parseDouble(strValue);
        }

        return dDefaultValue;
    } // getNodeValue()

    public static String getNodeValue(final NodeList nodes,
            final String strNodeName, final String strDefaultValue) {
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Node node = nodes.item(i);

            if (node instanceof Element) {
                final Element elem = (Element) node;

                if (elem.getNodeName().equals(strNodeName)) {
                    return elem.getLastChild().getTextContent().trim();
                }
            }
        } // end for

        return strDefaultValue;
    } // getValue()

    public static int getValue(final Node node, final String strAttrName,
            final String strAttrValue, final int iDefaultValue) {
        final String strValue = getValue(node.getChildNodes(), strAttrName,
                strAttrValue);

        if (strValue == null) {
            return iDefaultValue;
        }

        return Integer.parseInt(strValue);
    } // getValue()

    public static double getValue(final Node node, final String strAttrName,
            final String strAttrValue, final double dDefaultValue) {
        final String strValue = getValue(node.getChildNodes(), strAttrName,
                strAttrValue);

        if (strValue == null) {
            return dDefaultValue;
        }

        return Double.parseDouble(strValue);
    } // getValue()

    public static String getValue(final Node node, final String strAttrName,
            final String strAttrValue) {
        return getValue(node.getChildNodes(), strAttrName, strAttrValue);
    } // getValue()

    public static String getValue(final NodeList nodes,
            final String strAttrName, final String strAttrValue) {
        for (int i = 0; i < nodes.getLength(); ++i) {
            final Node node = nodes.item(i);

            if (node instanceof Element) {
                final Element elem = (Element) node;

                if (strAttrValue.equals(elem.getAttribute(strAttrName))) {
                    return elem.getLastChild().getTextContent().trim();
                }
            }
        } // end for

        return null;
    } // getValue()
} // end class XmlHelper
