/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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