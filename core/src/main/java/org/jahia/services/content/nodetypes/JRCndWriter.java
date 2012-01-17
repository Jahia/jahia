/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.nodetypes;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.version.OnParentVersionAction;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Prints node type defs in a compact notation
 * Print Format:
 * <ex = "http://apache.org/jackrabbit/example">
 * [ex:NodeType] > ex:ParentType1, ex:ParentType2
 * orderable mixin
 *   - ex:property (STRING) = 'default1', 'default2'
 *     primary mandatory autocreated protected multiple VERSION
 *     < 'constraint1', 'constraint2'
 *   + ex:node (ex:reqType1, ex:reqType2) = ex:defaultType
 *     mandatory autocreated protected multiple VERSION
 */
public class JRCndWriter {

    /**
     * the indention string
     */
    private static final String INDENT = "  ";

    /**
     * the underlying writer
     */
    private Writer out;

    /**
     * namespaces(prefixes) that are used
     */
    private Set usedNamespaces = new HashSet();

    private NodeTypeManager ntManager;

    public JRCndWriter(Writer out) {
        this.out = out;
    }

    public JRCndWriter(List<ExtendedNodeType> nti, Map<String,String> namespaces, Writer out, NodeTypeManager ntManager) throws IOException {
        this.out = out;
        this.ntManager = ntManager;
        writeNamespaces(namespaces);
        for (ExtendedNodeType ntd : nti) {
            write(ntd);
        }
    }

    public JRCndWriter(Collection<ExtendedNodeType> l, Writer out) throws IOException {
        this.out = out;

        for (ExtendedNodeType aL : l) {
            write(aL);
        }
    }

    /**
     * Flushes all pending write operations and Closes this writer. please note,
     * that the underlying writer remains open.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        out.flush();
        out.close();
        out = null;
    }

    private void writeNamespaces(Map<String,String> namespaces) throws IOException {
        for (Map.Entry<String,String> k : namespaces.entrySet()) {
            out.write("< "+k + " = '"+k.getValue()+"' >\n");
        }
    }

    /**
     * Write one NodeTypeDefExtension to this writer
     *
     * @param d
     * @throws IOException
     */
    public void write(ExtendedNodeType d) throws IOException {
        if (d.getName().startsWith("nt:") || d.getName().startsWith("mix:")) {
            try {
                if (ntManager.getNodeType(d.getName()) != null) {
                    return;
                }
            } catch (RepositoryException e) {
            }
        }
        writeName(d);
        writeSupertypes(d);
        writeOptions(d);
        writeItemDefExtensions(d);
        out.write("\n\n");
    }

    /**
     * write name
     */
    private void writeName(ExtendedNodeType ntd) throws IOException {
        out.write("[");
        out.write(ntd.getName());
        out.write("]");
    }

    /**
     * write supertypes
     */
    private void writeSupertypes(ExtendedNodeType ntd) throws IOException {
        ExtendedNodeType[] sta = ntd.getDeclaredSupertypes();
        String delim = " > ";
        for (int i = 0; i < sta.length; i++) {
            out.write(delim);
            out.write(sta[i].getName());
            delim = ", ";
        }

        for (ExtendedPropertyDefinition definition : ntd.getPropertyDefinitions()) {
            if (definition.isInternationalized()) {
                out.write(delim);
                out.write("jmix:i18n");
                break;
            }
        }


    }

    /**
     * write options
     */
    private void writeOptions(ExtendedNodeType ntd) throws IOException {
        if (ntd.hasOrderableChildNodes()) {
            out.write("\n" + INDENT);
            out.write("orderable");
            if (ntd.isMixin()) {
                out.write(" mixin");
            }
        } else if (ntd.isMixin()) {
            out.write("\n" + INDENT);
            out.write("mixin");
        }
    }

    /**
     * write prop defs
     */
    private void writeItemDefExtensions(ExtendedNodeType ntd) throws IOException {
        List items = ntd.getDeclaredItems();
        for (Iterator iterator = items.iterator(); iterator.hasNext();) {
            ExtendedItemDefinition itemDefExtension = (ExtendedItemDefinition) iterator.next();
            if (itemDefExtension.isNode()) {
                writeNodeDefExtension(ntd, (ExtendedNodeDefinition) itemDefExtension);
            } else {
                writePropertyDefExtension(ntd, (ExtendedPropertyDefinition) itemDefExtension);
            }
        }
    }

    /**
     * write prop def
     * @param pd
     */
    private void writePropertyDefExtension(ExtendedNodeType ntd, ExtendedPropertyDefinition pd) throws IOException {
        out.write("\n" + INDENT + "- ");
        out.write(pd.getName());
        out.write(" (");
        int i = pd.getRequiredType();
        if (i == ExtendedPropertyType.WEAKREFERENCE) { i = PropertyType.STRING; }
        if (i == ExtendedPropertyType.URI) { i = PropertyType.STRING; }
        if (i == ExtendedPropertyType.DECIMAL) { i = PropertyType.STRING; }

        out.write(PropertyType.nameFromValue(i).toLowerCase());
        out.write(")");
        writeDefaultValues(pd.getDefaultValues());
        out.write(ntd.getPrimaryItemName() != null && ntd.getPrimaryItemName().equals(pd.getName()) ? " primary" : "");
        if (pd.isMandatory() && !pd.isInternationalized()) {
            out.write(" mandatory");
        }
        if (pd.isMultiple()) {
            out.write(" multiple");
        }
        if (pd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (pd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(pd.getOnParentVersion()).toLowerCase());
        }

//        writeValueConstraints(pd.getValueConstraintsAsUnexpandedValue());

//        } else {
//            out.write("\n" + INDENT + "- ");
//            out.write(pd.getName() + "Title");
//            out.write(" (" +PropertyType.TYPENAME_STRING + ") multiple");
//            out.write("\n" + INDENT + "- ");
//            out.write(pd.getName() + "Reference");
//            out.write(" (" +PropertyType.TYPENAME_REFERENCE + ")");
//            out.write("\n" + INDENT + "- ");
//            out.write(pd.getName() + "URL");
//            out.write(" (" +PropertyType.TYPENAME_STRING + ")");
//        }
    }

    /**
     * write default values
     * @param dva
     */
    private void writeDefaultValues(Value[] dva) throws IOException {
        if (dva != null && dva.length > 0) {
            String delim = " = '";
            for (int i = 0; i < dva.length; i++) {
                try {
                    if (!(dva[i] instanceof DynamicValueImpl) && dva[i].getString() != null) {
                        out.write(delim);
                        out.write(escape(dva[i].getString()));
                        out.write("'");
                        delim = ", '";
                    }
                } catch (RepositoryException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }

    /**
     * write value constraints
     * @param vca
     */
    private void writeValueConstraints(Value[] vca) throws IOException {
        if (vca != null && vca.length > 0) {
            String delim = " < '";
            for (int i = 0; i < vca.length; i++) {
                try {
                    if (!(vca[i] instanceof DynamicValueImpl) && vca[i].getString() != null) {
                        out.write(delim);
                        out.write(escape(vca[i].getString()));
                        out.write("'");
                        delim = ", '";
                    }
                } catch (RepositoryException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }

    /**
     * write node def
     * @param nd
     */
    private void writeNodeDefExtension(ExtendedNodeType ntd, ExtendedNodeDefinition nd) throws IOException {
        out.write("\n" + INDENT + "+ ");

        String name = nd.getName();
        out.write(name);
        writeRequiredTypes(nd.getRequiredPrimaryTypes());
        writeDefaultType(nd.getDefaultPrimaryType());
        out.write(ntd.getPrimaryItemName() != null && ntd.getPrimaryItemName().equals(nd.getName()) ? " primary" : "");
        if (nd.isMandatory() && !nd.isAutoCreated()) {
            out.write(" mandatory");
        }
        if (nd.isProtected()) {
            out.write(" protected");
        }
        if (nd.isAutoCreated()) {
//            out.write(" autocreated");
        }
        if (nd.allowsSameNameSiblings()) {
            out.write(" multiple");
        }
        if (nd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(nd.getOnParentVersion()).toLowerCase());
        }
    }

    /**
     * write required types
     * @param reqTypes
     */
    private void writeRequiredTypes(NodeType[] reqTypes) throws IOException {
        if (reqTypes != null && reqTypes.length > 0) {
            String delim = " (";
            for (int i = 0; i < reqTypes.length; i++) {
                out.write(delim);
                out.write(reqTypes[i].getName());
                delim = ", ";
            }
            out.write(")");
        }
    }

    /**
     * write default types
     * @param defType
     */
    private void writeDefaultType(NodeType defType) throws IOException {
        if (defType != null && !defType.getName().equals("*")) {
            out.write(" = ");
            out.write(defType.getName());
        }
    }

    /**
     * escape
     * @param s
     * @return the escaped string
     */
    private String escape(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\\') {
                sb.insert(i, '\\');
                i++;
            } else if (sb.charAt(i) == '\'') {
                sb.insert(i, '\'');
                i++;
            }
        }
        return sb.toString();
    }
}
