package org.jahia.services.importexport;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 21, 2009
 * Time: 11:31:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLFormatDetectionHandler extends DefaultHandler {

    public static final int JCR_SYSVIEW = 1;
    public static final int JCR_DOCVIEW = 2;
    public static final int JAHIA_CONTENT = 3;
    public static final int USERS = 4;
    public static final int CATEGORIES = 5;

    private Map<String,String> prefixMapping = new HashMap<String, String>();

    private int type = -1;

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMapping.put(prefix,  uri);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (localName.equals("page") && uri.equals("http://www.jahia.org/")) {
            type = JAHIA_CONTENT;
        } else if (localName.equals("categories") && uri.equals("http://www.jahia.org/")) {
            type = CATEGORIES;
        } else if (localName.equals("users") && uri.equals("http://www.jahia.org/")) {
            type = USERS;
        } else if (prefixMapping.containsValue("http://www.jcp.org/jcr/1.0")) {
            type = JCR_DOCVIEW;
        }

        throw new SAXException("Found");
    }

    public int getType() {
        return type;
    }
}
