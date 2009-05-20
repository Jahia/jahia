/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.importexport;

import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;
import org.jahia.engines.EngineMessage;
import org.xml.sax.Attributes;

/**
 * Created by IntelliJ IDEA.
 * Date: 15 nov. 2005 - 13:12:06
 *
 * @author toto
 * @version $Id$
 */
public class NodeImportResult extends NodeOperationResult {
    private static final long serialVersionUID = 8871772764958729362L;

    private Throwable t;
    private String namespace;
    private String localName;
    private String qname;
    private String importString;

    public NodeImportResult(ObjectKey nodeKey, String languageCode, EngineMessage msg, String localName, String namespace, String qname, Attributes attr, Throwable t) {
        super(nodeKey, languageCode, null, msg);
        this.localName = localName;
        this.namespace = namespace;
        this.qname = qname;

        StringBuffer b = new StringBuffer();
        b.append("<");
        b.append(qname);
        if (attr != null) {
            int length = attr.getLength();
            for (int i=0; i<length;i++) {
                b.append(" ");
                b.append(attr.getQName(i));
                b.append("=\"");
                b.append(attr.getValue(i));
                b.append("\"");
            }
        }
        b.append(">");
        this.importString = b.toString();
        this.t = t;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLocalName() {
        return localName;
    }

    public String getQname() {
        return qname;
    }

    public String getImportString() {
        return importString;
    }

    public Throwable getException() {
        return t;
    }

}
/**
 *$Log $
 */