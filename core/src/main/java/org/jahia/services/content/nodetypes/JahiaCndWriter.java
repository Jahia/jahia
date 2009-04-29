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
package org.jahia.services.content.nodetypes;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
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
public class JahiaCndWriter {

    /**
     * the indention string
     */
    private static final String INDENT = "  ";

    /**
     * the underlying writer
     */
    private Writer out;

    public JahiaCndWriter(Writer out) {
        this.out = out;
    }

    public JahiaCndWriter(NodeTypeIterator nti, Map<String,String> namespaces, Writer out) throws IOException {
        this.out = out;
        writeNamespaces(namespaces);
        while (nti.hasNext()) {
            ExtendedNodeType ntd = (ExtendedNodeType) nti.nextNodeType();
            write(ntd);
        }
    }

    public JahiaCndWriter(Collection<ExtendedNodeType> l, Writer out) throws IOException {
        this.out = out;

        for (ExtendedNodeType aL : l) {
            write(aL);
        }
    }

    /**
     * Flushes all pending write operations and Closes this writer. please note,
     * that the underlying writer remains open.
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        out.flush();
        out.close();
        out = null;
    }

    private void writeNamespaces(Map<String,String> namespaces) throws IOException {
        for (String k : namespaces.keySet()) {
            out.write("< "+k + " = '"+namespaces.get(k)+"' >\n");
        }
    }

    /**
     * Write one NodeTypeDefExtension to this writer
     *
     * @param d
     * @throws java.io.IOException
     */
    public void write(ExtendedNodeType d) throws IOException {
        if (d.getName().startsWith("nt:") || d.getName().startsWith("mix:")) {
            return;
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
        if (ntd.isAbstract()) {
            out.write("\n" + INDENT);
            out.write("abstract");            
        }
        if (ntd.getValidator() != null) {
            out.write("\n" + INDENT);
            out.write("validator='");
            out.write(escape(ntd.getValidator()));
            out.write("'");
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
        out.write(PropertyType.nameFromValue(pd.getRequiredType()).toLowerCase());
        out.write(",");
        out.write(SelectorType.nameFromValue(pd.getSelector()).toLowerCase());
        out.write(")");
        writeDefaultValues(pd.getDefaultValues());
        out.write(ntd.getPrimaryItemName() != null && ntd.getPrimaryItemName().equals(pd.getName()) ? " primary" : "");
        if (pd.isMandatory()) {
            out.write(" mandatory");
        }
        if (pd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (pd.isProtected()) {
            out.write(" protected");
        }
        if (pd.isMultiple()) {
            out.write(" multiple");
        }
        if (pd.isInternationalized()) {
            out.write(" internationalized");
        }

        out.write(" indexed=");
        switch (pd.getIndex()) {
            case ExtendedPropertyDefinition.INDEXED_NO:
                out.write("no");
                break;
            case ExtendedPropertyDefinition.INDEXED_TOKENIZED:
                out.write("tokenized");
                break;
            case ExtendedPropertyDefinition.INDEXED_UNTOKENIZED:
                out.write("untokenized");
                break;
        }

        if (pd.getScoreboost() != 1.) {
            out.write(" scoreboost="+pd.getScoreboost());
        }
        if ( pd.getAnalyzer() != null) {
            out.write(" analyzer="+pd.getAnalyzer());
        }
        if (pd.getOnParentVersion() != OnParentVersionAction.COPY) {
            out.write(" ");
            out.write(OnParentVersionAction.nameFromValue(pd.getOnParentVersion()).toLowerCase());
        }

        writeValueConstraints(pd.getValueConstraintsAsValue());
    }

    /**
     * write default values
     * @param dva
     */
    private void writeDefaultValues(Value[] dva) throws IOException {
        if (dva != null && dva.length > 0) {
            String delim = " = ";
            writeValueList(dva, delim);
        }
    }

    /**
     * write value constraints
     * @param vca
     */
    private void writeValueConstraints(Value[] vca) throws IOException {
        if (vca != null && vca.length > 0) {
            String delim = " < ";
            writeValueList(vca, delim);
        }
    }

    private void writeValueList(Value[] dva, String delim) throws IOException {
        for (int i = 0; i < dva.length; i++) {
            try {
                if (dva[i] instanceof DynamicValueImpl) {
                    out.write(delim);
                    out.write(escape(((DynamicValueImpl)dva[i]).getFn()));
                    out.write("( ");
                    List<String> p = ((DynamicValueImpl)dva[i]).getParams();
                    for (String s : p) {
                        out.write(s);
                        out.write(" ");
                    }
                    out.write(")");
                    delim = ", ";
                } else if (dva[i].getString() != null) {
                    out.write(delim);
                    out.write("'");
                    out.write(escape(dva[i].getString()));
                    out.write("'");
                    delim = ", ";
                }
            } catch (RepositoryException e) {
                throw new IOException(e.getMessage());
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
        if (nd.isMandatory()) {
            out.write(" mandatory");
        }
        if (nd.isAutoCreated()) {
            out.write(" autocreated");
        }
        if (nd.isProtected()) {
            out.write(" protected");
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