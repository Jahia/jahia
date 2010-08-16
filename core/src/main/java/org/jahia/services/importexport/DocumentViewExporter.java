/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.importexport;

import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.value.ValueHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.jcr.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 17, 2009
 * Time: 3:02:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentViewExporter {
    private static final String CDATA = "CDATA";
    private static final String NS_URI = "http://www.w3.org/2000/xmlns/";

    private JCRSessionWrapper session;
    private ContentHandler ch;
    private boolean noRecurse;
    private boolean skipBinary;
    private Set<String> typesToIgnore = new HashSet<String>();
    private Map<String, String> prefixes;
    private HashMap<String, String> exportedShareable;
    private JCRNodeWrapper rootNode;
    private Stack<String> stack;

    private List<String> excluded = Arrays.asList("jcr:predecessors", "j:originWS", "j:nodename", "jcr:versionHistory", "jcr:baseVersion", "jcr:isCheckedOut");

    public DocumentViewExporter(JCRSessionWrapper session, ContentHandler ch, boolean skipBinary, boolean noRecurse) {
        this.session = session;
        this.ch = ch;
        this.noRecurse = noRecurse;
        this.skipBinary = skipBinary;

        this.stack = new Stack<String>();

        prefixes = new HashMap<String, String>();
        prefixes.put(Constants.NT_PREF, Constants.NT_NS);
        prefixes.put(Constants.JCR_PREF, Constants.JCR_NS);
        prefixes.put(Constants.MIX_PREF, Constants.MIX_NS);
        prefixes.put(Constants.JAHIANT_PREF, Constants.JAHIANT_NS);
        prefixes.put(Constants.JAHIA_PREF, Constants.JAHIA_NS);
        prefixes.put(Constants.JAHIAMIX_PREF, Constants.JAHIAMIX_NS);

        exportedShareable = new HashMap<String, String>();
    }

    public void setTypesToIgnore(Set<String> typesToIgnore) {
        this.typesToIgnore = typesToIgnore;
    }


    public void export(JCRNodeWrapper node) throws SAXException, RepositoryException {
        TreeSet<JCRNodeWrapper> set = new TreeSet<JCRNodeWrapper>();
        set.add(node);
        export(node, set);
    }

    public void export(JCRNodeWrapper rootNode, SortedSet<JCRNodeWrapper> files) throws SAXException, RepositoryException {
        this.rootNode = rootNode;

        ch.startDocument();

        for (Iterator<JCRNodeWrapper> iterator = files.iterator(); iterator.hasNext();) {
            JCRNodeWrapper node = iterator.next();
            exportNode(node);
        }
        while (!stack.isEmpty()) {
            String end = stack.pop();
            String name = end.substring(end.lastIndexOf('/') + 1);
            String encodedName = ISO9075.encode(name);
            endElement(encodedName);
        }

        ch.endDocument();
    }

    private void exportNode(JCRNodeWrapper node) throws SAXException, RepositoryException {
        if (node.getProvider().isExportable() && !typesToIgnore.contains(node.getPrimaryNodeTypeName())) {

            String path = "";
            Node current = node;
            while (!current.getPath().equals("/")) {
                path = "/" + current.getName() + path;
                current = current.getParent();
            }
            if (path.equals("")) {
                path = "/";
            }

            if (!path.equals(rootNode.getPath())) {

                String parentpath = path.substring(0, path.lastIndexOf('/'));

                while (!stack.isEmpty() && !parentpath.startsWith(stack.peek())) {
                    String end = stack.pop();
                    if (stack.isEmpty()) {
                        throw new RepositoryException("Node not in path");
                    }
                    String name = end.substring(end.lastIndexOf('/') + 1);
                    String encodedName = ISO9075.encode(name);
                    endElement(encodedName);
                }
                if (stack.isEmpty() && !node.getPath().equals(rootNode.getPath())) {
                    String name = rootNode.getName();
                    String encodedName = ISO9075.encode(name);
                    AttributesImpl atts = new AttributesImpl();
                    startElement(encodedName, atts);
                    if (rootNode.getPath().equals("/")) {
                        stack.push("");
                    } else {
                        stack.push(rootNode.getPath());
                    }
                }

                while (!stack.isEmpty() && !stack.peek().equals(parentpath)) {
                    String peek = stack.peek();
                    String name = parentpath.substring(peek.length() + 1);
                    if (name.contains("/")) {
                        name = name.substring(0, name.indexOf('/'));
                    }
                    String encodedName = ISO9075.encode(name);
                    String currentpath = peek + "/" + name;
                    String pt = session.getNode(currentpath).getPrimaryNodeTypeName();
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute(Constants.JCR_NS, "primaryType", "jcr:primaryType", CDATA, pt);
                    startElement(encodedName, atts);
                    stack.push(currentpath);
                }
            }
            AttributesImpl atts = new AttributesImpl();
            if (node.isNodeType("jmix:shareable")) {

                if (exportedShareable.containsKey(node.getIdentifier())) {
                    atts.addAttribute(Constants.JAHIA_NS,  "share", "j:share", CDATA, exportedShareable.get(node.getIdentifier()));
                    String encodedName = ISO9075.encode(node.getName());
                    startElement(encodedName, atts);
                    endElement(encodedName);
                    return;
                } else {
                    exportedShareable.put(node.getIdentifier(), node.getPath());
                }                
            }
            PropertyIterator propsIterator = node.getProperties();
            while (propsIterator.hasNext()) {
                JCRPropertyWrapper property = (JCRPropertyWrapper) propsIterator.nextProperty();
                if ((property.getType() != PropertyType.BINARY || !skipBinary) && !excluded.contains(property.getName())) {
                    String key = property.getName();
                    String prefix = null;
                    String localname = key;
                    if (key.indexOf(':') > -1) {
                        prefix = key.substring(0, key.indexOf(':'));
                        localname = key.substring(key.indexOf(':') + 1);
                    }

                    String attrName = ISO9075.encode(localname);

                    String value;
                    if (!property.isMultiple()) {
                        value = getValue(property.getValue());
                    } else {
                        Value[] vs = property.getValues();
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < vs.length; i++) {
                            Value v = vs[i];
                            b.append(getValue(v));
                            if (i + 1 < vs.length) {
                                b.append(" ");
                            }
                        }
                        value = b.toString();
                    }

                    if (prefix == null) {
                        atts.addAttribute("", localname, attrName, CDATA, value);
                    } else {
                        atts.addAttribute(prefixes.get(prefix), localname, prefix + ":" + attrName, CDATA, value);
                    }
                }

            }

            String encodedName = ISO9075.encode(node.getName());
            startElement(encodedName, atts);
            if (path.equals("/")) {
                stack.push("");
            } else {
                stack.push(path);
            }

            if (!noRecurse) {
                NodeIterator ni = node.getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper c = (JCRNodeWrapper) ni.next();
                    exportNode(c);
                }
            }
        }
    }

    private String getValue(Value v) throws RepositoryException {
        if (v.getType() == PropertyType.REFERENCE || v.getType() == PropertyType.WEAKREFERENCE) {
            try {
                return ISO9075.encodePath(session.getNodeByUUID(v.getString()).getPath()); 
            } catch (ItemNotFoundException e) {
                return "";
            }
        } else if (v.getType() == PropertyType.BINARY) {
            return ValueHelper.serialize(v, false);
        }

        return v.getString();
    }

    private void startElement(String qualifiedName, AttributesImpl atts) throws SAXException {
        if (qualifiedName.equals("")) {
            qualifiedName = "content";
        }

        if (stack.isEmpty()) {
            for (Iterator<String> iterator = prefixes.keySet().iterator(); iterator.hasNext();) {
                String prefix = iterator.next();
                String uri = prefixes.get(prefix);
                atts.addAttribute(NS_URI, prefix, "xmlns:" + prefix, CDATA, uri);
                ch.startPrefixMapping(prefix, uri);
                ch.endPrefixMapping(prefix);
            }
        }

        ch.startElement("", qualifiedName, qualifiedName, atts);
    }

    private void endElement(String qualifiedName) throws SAXException {
        if (qualifiedName.equals("")) {
            qualifiedName = "content";
        }
        ch.endElement("", qualifiedName, qualifiedName);
    }


}
